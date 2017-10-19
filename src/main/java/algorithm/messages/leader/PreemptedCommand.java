package algorithm.messages.leader;

public class PreemptedCommand implements LeaderCommand {
    public int ballotNumber;

    public PreemptedCommand(int ballotNumber) {
        this.ballotNumber = ballotNumber;
    }
}
