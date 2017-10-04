package algorithm.messages.acceptor;

public class P2B extends PB {
    public int commanderId;
    public P2B() {
        super(0, 0);
    }

    public P2B(int ballotNumber, int acceptor, int commanderId) {
        super(ballotNumber, acceptor);
        this.commanderId = commanderId;
    }

    @Override
    public String toString() {
        return String.format("p2b{ ballot=%d, acceptor=%d }", ballotNumber, acceptor);
    }
}