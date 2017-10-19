package algorithm.messages.replica;

import java.util.Objects;

public class Propose {
    public int slot;
    public Command command;

    public Propose(int slot, Command command) {
        this.slot = slot;
        this.command = command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Propose propose = (Propose) o;
        return slot == propose.slot &&
                Objects.equals(command, propose.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slot, command);
    }

    @Override
    public String toString() {
        return "Propose on slot " + slot + " with command " +  command;
    }
}
