package algorithm.leader;

import algorithm.acceptor.BallotProposal;
import algorithm.messages.acceptor.P2A;
import algorithm.messages.acceptor.P2B;
import algorithm.messages.leader.PreemptedCommand;
import algorithm.messages.replica.Decision;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.List;
import java.util.Set;

public class Commander extends LeaderSlave {
    public final BallotProposal ballotProposal;

    public Commander(Set<Integer> acceptors, BallotProposal ballotProposal) {
        super(acceptors);
        this.ballotProposal = ballotProposal;
    }
}