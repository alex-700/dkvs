package algorithm.messages.acceptor;

import algorithm.acceptor.BallotProposal;

import java.util.HashSet;
import java.util.Set;

public class P1B extends PB {
    public Set<BallotProposal> accepts;

    public P1B() {
        super(0, 0);
    }

    public P1B(int ballotNumber, Set<BallotProposal> accepts, int acceptor) {
        super(ballotNumber, acceptor);
        this.accepts = new HashSet<>(accepts);
    }

    @Override
    public String toString() {
        return String.format("P1B { bal: %d, acc: %d, accepts: %s}", ballotNumber, acceptor, accepts);
    }
}