package algorithm.acceptor;

import algorithm.messages.replica.Command;
import java.util.Objects;

public class BallotProposal implements Comparable<BallotProposal> {

    public int ballot, slot;
    public Command command;

    public BallotProposal() {}

    public BallotProposal(int ballot, int slot, Command command) {
        this.ballot = ballot;
        this.slot = slot;
        this.command = command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BallotProposal that = (BallotProposal) o;
        return ballot == that.ballot &&
                slot == that.slot &&
                Objects.equals(command, that.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ballot, slot, command);
    }

    @Override
    public String toString() {
        return String.format("{bal: %d, slot: %d, command: '%s'}", ballot, slot, command);
    }

    @Override
    public int compareTo(BallotProposal o) {
        if (ballot != o.ballot) {
            return Integer.compare(ballot, o.ballot);
        } else if (slot != o.slot) {
            return Integer.compare(slot, o.slot);
        } else {
            return 0;
        }
    }
}