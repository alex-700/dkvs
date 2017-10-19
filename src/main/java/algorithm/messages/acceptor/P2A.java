package algorithm.messages.acceptor;

import algorithm.acceptor.BallotProposal;

public class P2A extends PA {
    public int commanderId;
    public BallotProposal ballotProposal;

    public P2A(int lambda, BallotProposal ballotProposal, int commanderId) {
        super(lambda);
        this.ballotProposal = ballotProposal;
        this.commanderId = commanderId;
    }

    @Override
    public String toString() {
        return String.format("P2A { lam: %d balProp: %s commander: %d }", lambda, ballotProposal, commanderId);
    }
}
