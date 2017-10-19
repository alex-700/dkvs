package algorithm.messages.leader;

import algorithm.acceptor.BallotProposal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AdoptedCommand implements LeaderCommand {
    private int ballotNumber;
    public Set<BallotProposal> pvalues;

    public AdoptedCommand(int ballotNumber, Collection<BallotProposal> pvalues) {
        this.ballotNumber = ballotNumber;
        this.pvalues = new HashSet<>(pvalues);
    }

    @Override
    public String toString() {
        return String.format("AdoptedCommand{ballotNumber=%d, pvalues=%s}", ballotNumber, pvalues);
    }
}
