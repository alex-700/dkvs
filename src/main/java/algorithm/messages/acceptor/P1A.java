package algorithm.messages.acceptor;

public class P1A extends PA {
    public int ballot;

    public P1A() {
        super(0);
    }

    public P1A(int lambda, int ballot) {
        super(lambda);
        this.ballot = ballot;
    }

    @Override
    public String toString() {
        return String.format("P1A { lam: %d, ballot: %d }", lambda, ballot);
    }
}