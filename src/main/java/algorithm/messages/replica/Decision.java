package algorithm.messages.replica;

public class Decision {
    public int slotNumber;
    public Command command;

    public Decision() {}

    public Decision(int slotNumber, Command command) {
        this.slotNumber = slotNumber;
        this.command = command;
    }

    @Override
    public String toString() {
        return String.format("Decision on slot %d, command -> %s", slotNumber, command);
    }
}