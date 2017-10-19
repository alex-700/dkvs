package algorithm.messages.acceptor;

public abstract class PB implements AcceptorCommand {
    public int ballotNumber;
    public int acceptor;

    PB(int ballotNumber, int acceptor) {
        this.ballotNumber = ballotNumber;
        this.acceptor = acceptor;
    }
}
