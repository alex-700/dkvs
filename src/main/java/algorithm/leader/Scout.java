package algorithm.leader;

import algorithm.acceptor.BallotProposal;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

class Scout extends LeaderSlave {
    final ConcurrentSkipListSet<BallotProposal> pvalues = new ConcurrentSkipListSet<>();
    final int ballotNumber;

    Scout(Set<Integer> acceptors, int ballotNumber) {
        super(acceptors);
        this.ballotNumber = ballotNumber;
    }
}
