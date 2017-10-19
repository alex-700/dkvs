package algorithm.leader;

import algorithm.Utils;
import algorithm.acceptor.BallotProposal;
import algorithm.messages.acceptor.P1A;
import algorithm.messages.acceptor.P1B;
import algorithm.messages.acceptor.P2A;
import algorithm.messages.acceptor.P2B;
import algorithm.messages.leader.AdoptedCommand;
import algorithm.messages.leader.PreemptedCommand;
import algorithm.messages.replica.Decision;
import algorithm.messages.replica.Propose;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Leader {
    private static final int TIMEOUT_TIME = 3;
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    private final int id;
    private final List<Client> toAcceptors;
    private final Set<Integer> acceptors;
    private int ballotNumber;
    private boolean active;
    private Set<Propose> proposals;
    private Scout scout;
    private Map<Integer, Commander> commanders;
    private Random rnd;

    private AtomicBoolean scoutAlive = new AtomicBoolean(false);
    private AtomicBoolean commanderAlive = new AtomicBoolean(false);

    private final BlockingDeque<Decision>[] qs;
    private final Condition[] conditions;
    private final ReentrantLock[] locks;

    @SuppressWarnings("unchecked")
    Leader(int id, int countLeaders, Server server,
           List<Client> toAcceptors, List<Client> toReplicas,
           Set<Integer> acceptors, Client clientToLeader) {
        Thread[] threads = new Thread[toReplicas.size()];
        qs = new BlockingDeque[toReplicas.size()];
        locks = new ReentrantLock[toReplicas.size()];
        conditions = new Condition[toReplicas.size()];

        Arrays.setAll(qs, i -> new LinkedBlockingDeque<>());
        Arrays.setAll(locks, i -> new ReentrantLock());
        Arrays.setAll(conditions, i -> locks[i].newCondition());

        for (int it = 0; it < threads.length; it++) {
            final int i = it;
            threads[i] = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Decision d = qs[i].takeFirst();
                        toReplicas.get(i).sendTCP(d);
                        locks[i].lock();
                        if (!conditions[i].await(TIMEOUT_TIME, TIMEOUT_UNIT)) {
                            System.err.println("Fail and reconnect");
                            qs[i].addFirst(d);
                            if (!toReplicas.get(i).isConnected()) {
                                toReplicas.get(i).reconnect();
                            }
                        }
                        locks[i].unlock();
                    } catch (InterruptedException e) {
                        break;
                    } catch (IOException ignored) {}
                }
            });
            threads[i].start();
        }

        this.id = id;
        ballotNumber = id; // smth with id
        this.toAcceptors = toAcceptors;
        this.acceptors = acceptors;
        active = false;
        proposals = new HashSet<>();
        commanders = new HashMap<>();
        rnd = new Random(15082005);

        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object o) {
                if (o instanceof Propose) {
                    Propose propose = (Propose) o;
                    System.out.println("I got propose " + propose.command);
                    synchronized (Leader.this) {
                        proposals.add(propose);
                    }
                    if (active) {
                        startCommander(propose);
                    }
                } else if (o instanceof AdoptedCommand) {
                    System.out.println("got adopted command");
                    System.out.println("proposals -> " + proposals);
                    AdoptedCommand adopted = (AdoptedCommand) o;
                    triangle(pmax(adopted.pvalues));
                    System.out.println("proposals -> " + proposals);
                    synchronized (Leader.this) {
                        proposals.forEach(propose -> startCommander(propose));
                    }
                    active = true;
                } else if (o instanceof PreemptedCommand) {
                    PreemptedCommand preempted = (PreemptedCommand) o;
                    System.out.println("got preempted command " + preempted.ballotNumber + " " + ballotNumber);
                    if (preempted.ballotNumber > ballotNumber) {
                        active = false;
                        ballotNumber += countLeaders;
                        startScout();
                    }
                }
            }
        });

        for (int i = 0; i < toReplicas.size(); i++) {
            int finalI = i;
            toReplicas.get(i).addListener(new Listener() {
                @Override
                public void received(Connection connection, Object o) {
                    if ("Acknowledge".equals(o)) {
                        locks[finalI].lock();
                        conditions[finalI].signalAll();
                        locks[finalI].unlock();
                    }
                }
            });
        }
        toAcceptors.forEach(client -> client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object o) {
                if (scoutAlive.get()) {
                    if (o instanceof P1B) {
                        P1B p1B = (P1B) o;
                        System.out.println("our balllot = " + scout.ballotNumber + " I got " + p1B);
                        if (p1B.ballotNumber == scout.ballotNumber) {
                            scout.pvalues.addAll(p1B.accepts);
                            scout.waitors.remove(p1B.acceptor);
                            if (scout.waitors.size() <= acceptors.size() / 2) {
                                if (scoutAlive.compareAndSet(true, false)) {
                                    Utils.send(clientToLeader, new AdoptedCommand(scout.ballotNumber, scout.pvalues));
//                                        clientToLeader.sendTCP(new AdoptedCommand(scout.ballotNumber, scout.pvalues));
                                }
                            }
                        } else {
                            System.out.println("send preempted");
                            if (scoutAlive.compareAndSet(true, false)) {
                                Utils.send(clientToLeader, new PreemptedCommand(p1B.ballotNumber));
//                                    clientToLeader.sendTCP(new PreemptedCommand(p1B.ballotNumber));
                            }
                        }
                    }
                } else if (commanderAlive.get()) {
                    if (o instanceof P2B) {
                        P2B p2B = (P2B) o;
                        System.out.println("get p2b: " + p2B);
                        Commander commander = commanders.get(p2B.commanderId);
                        if (commander == null) return;
                        if (commander.ballotProposal.ballot == p2B.ballotNumber) {
                            commander.waitors.remove(p2B.acceptor);
                            if (commander.waitors.size() <= acceptors.size() / 2) {
                                commanders.remove(p2B.commanderId);
                                Arrays.stream(qs).forEach(q -> {
                                    System.out.println("offer to replicas");
                                    q.addLast(new Decision(commander.ballotProposal.slot, commander.ballotProposal.command));
                                });
                                if (commanders.isEmpty()) {
                                    commanderAlive.set(false);
                                }
                            }
                        } else {
                            if (commanderAlive.compareAndSet(true, false)) {
                                commanders.clear();
                                System.out.println("Send preempted");
                                Utils.send(clientToLeader, new PreemptedCommand(p2B.ballotNumber));
//                                    clientToLeader.sendTCP(new PreemptedCommand(p2B.ballotNumber));
                            }
                        }
                    }
                }
            }
        }));
        startScout();
    }

    private Set<Propose> pmax(Set<BallotProposal> pvals) {
        Set<Propose> ans = new HashSet<>();
        for (BallotProposal pval : pvals) {
            boolean ok = true;
            for (BallotProposal other : pvals) {
                if (pval.slot == other.slot && other.ballot > pval.ballot) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                ans.add(new Propose(pval.slot, pval.command));
            }
        }
        return ans;
    }

    private void triangle(Set<Propose> p) {
        Set<Propose> ans = new HashSet<>(p);
        for (Propose proposal : proposals) {
            boolean ok = true;
            for (Propose other : p) {
                if (proposal.slot == other.slot) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                ans.add(proposal);
            }
        }
        synchronized (this) {
            proposals = ans;
        }
    }

    private void startScout() {
        if (scoutAlive.compareAndSet(false, true)) {
            System.out.println("start Scout");
            scout = new Scout(acceptors, ballotNumber);
            toAcceptors.forEach(client ->
                Utils.send(client, new P1A(id, ballotNumber))
//                client.sendTCP(new P1A(id, ballotNumber));
            );
        }
    }

    private void startCommander(Propose propose) {
        int commanderId;
        //noinspection StatementWithEmptyBody
        while (commanders.containsKey(commanderId = rnd.nextInt()));
        final int finalCommanderId = commanderId;
        commanderAlive.set(true);
//        if (commanderAlive.compareAndSet(false, true)) {
        System.out.println("start commander on propose " + propose.command);
        Commander commander = new Commander(acceptors, new BallotProposal(ballotNumber, propose.slot, propose.command));
        commanders.put(finalCommanderId, commander);
        toAcceptors.forEach(client ->
            Utils.send(client, new P2A(id, commander.ballotProposal, finalCommanderId))
//                client.sendTCP(new P2A(id, commander.ballotProposal));
        );
//        }
    }

}
