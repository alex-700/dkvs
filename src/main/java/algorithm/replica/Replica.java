package algorithm.replica;

import algorithm.Pair;
import algorithm.Utils;
import algorithm.messages.replica.*;
import com.esotericsoftware.kryonet.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Replica {
    public static final int WINDOW = 200;

    private final Map<String, String> data;
    private int slotIn;
    private int slotOut;
    private Set<Command> requests;
    private Set<Pair<Integer, Command>> proposals;
    private Set<Decision> decisions;
    private final List<Client> toLeaders;
    private final Server server;
    private final int id;
    private final int countReplicas;
    private final Map<Integer, Connection> answer = new HashMap<>();
    private int commandCount;
    private final Client toYourself;
    private final FileLogger logger;

    int getCommandId() {
        int ans = commandCount * countReplicas + id;
        commandCount++;
        return ans;
    }

    public Replica(List<Client> toLeaders, Server server, int id, int countReplicas, Client toYourself) {
        this.toLeaders = toLeaders;
        this.server = server;
        this.id = id;
        this.countReplicas = countReplicas;
        this.toYourself = toYourself;
        toYourself.setKeepAliveTCP(1000); // botay message

        logger = new FileLogger(String.format("replica%d.log", id));
        data = new HashMap<>();
        int countCommandInLog = logger.load(data);
        slotIn = countCommandInLog + 1;
        slotOut = countCommandInLog + 1;
        requests = new HashSet<>();
        proposals = new HashSet<>();
        decisions = new HashSet<>();
        commandCount = countCommandInLog;

        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object o) {
                if (o instanceof Decision) {
                    System.out.println("Sending acknowledge on " + o);
                    connection.sendTCP("Acknowledge");
                    Decision d = (Decision) o;
                    decisions.add(d);
                    for (Decision decision : decisions) {
                        if (decision.slotNumber == slotOut) {
                            Set<Pair<Integer, Command>> nProposals = new HashSet<>();
                            for (Pair<Integer, Command> p : proposals) {
                                if (p.first == slotOut) {
                                    if (!decision.command.equals(p.second)) {
                                        requests.add(p.second);
                                    }
                                } else {
                                    nProposals.add(p);
                                }
                            }
                            System.out.println("slot = " + decision.slotNumber + " " + decision.command);
                            perform(decision.command);
                        }
                    }
                } else if (o instanceof String) {
                    String message = (String) o;
                    Scanner in = new Scanner(message);
                    String type = in.next();
                    switch (type) {
                        case "get": {
                            String x = in.next();
                            synchronized (data) {
                                String value = data.get(x);
                                if (value == null) {
                                    connection.sendTCP("NOT_FOUND");
                                } else {
                                    connection.sendTCP(String.format("VALUE %s %s", x, value));
                                }
                            }
                            break;
                        }
                        case "set": {
                            int commandId = getCommandId();
                            String key = in.next();
                            String value = in.next();
                            requests.add(new SetCommand(commandId, key, value));
                            answer.put(commandId, connection);
                            break;
                        }
                        case "delete": {
                            int commandId = getCommandId();
                            String key = in.next();
                            requests.add(new DeleteCommand(commandId, key));
                            answer.put(commandId, connection);
                            break;
                        }
                        case "ping": {
                            connection.sendTCP("PONG");
                            break;
                        }
                    }
                } else if (o instanceof FrameworkMessage.KeepAlive) {
                    propose();
                }
            }
        });
    }

    public boolean checkDecision() {
        for (Decision decision : decisions) {
            if (decision.slotNumber == slotIn) {
                return false;
            }
        }
        return true;
    }

    public void propose() {
        while (slotIn < slotOut + WINDOW && requests.size() != 0) {
            if (checkDecision()) {
                Command c = requests.toArray(new Command[requests.size()])[0];
                requests.remove(c);
                proposals.add(new Pair<>(slotIn, c));
                toLeaders.stream().forEach(client ->  {
                    System.out.println("send propose to leader" + slotIn + " " + c);
                    Utils.send(client, new Propose(slotIn, c));
//                    client.sendTCP(new Propose(slotIn, c));
                });
            }
            slotIn++;
        }
    }

    public void perform(Command c) {
        boolean flag = false;
        for (Decision d : decisions) {
            if (d.slotNumber < slotOut && d.command.equals(c)) {
                flag = true;
                break;
            }
        }
        synchronized (this) {
            if (!flag) {
                logger.log(c);
                System.out.println("new opertation " + c);
                if (c instanceof SetCommand) {
                    int cid = ((SetCommand) c).id;
                    if (cid % countReplicas == id && answer.containsKey(cid)) {
                        answer.get(cid).sendTCP("STORED");
                        answer.remove(cid);
                    }
                    data.put(((SetCommand) c).key, ((SetCommand) c).value);
                } else if (c instanceof DeleteCommand) {
                    int cid = ((DeleteCommand) c).id;
                    if (cid % countReplicas == id && answer.containsKey(cid)) {
                        answer.get(cid).sendTCP("DELETED");
                        answer.remove(cid);
                    }
                    data.remove(((DeleteCommand) c).key);
                }
            }
            slotOut++;
        }
    }
}