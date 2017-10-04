package algorithm.leader;

import algorithm.acceptor.BallotProposal;
import algorithm.messages.acceptor.P1B;
import algorithm.messages.acceptor.P1A;
import algorithm.messages.leader.AdoptedCommand;
import algorithm.messages.leader.PreemptedCommand;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class Scout extends LeaderSlave {
    public final ConcurrentSkipListSet<BallotProposal> pvalues = new ConcurrentSkipListSet<>();
    public final int ballotNumber;

    public Scout(Set<Integer> acceptors, int ballotNumber) {
        super(acceptors);
        this.ballotNumber = ballotNumber;
    }
}