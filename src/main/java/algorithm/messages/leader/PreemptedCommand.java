package algorithm.messages.leader;

public class PreemptedCommand implements LeaderCommand {
    public int ballotNumber;

    public PreemptedCommand() {}

    public PreemptedCommand(int ballotNumber) {
        this.ballotNumber = ballotNumber;
    }
}