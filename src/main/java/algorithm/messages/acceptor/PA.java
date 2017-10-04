package algorithm.messages.acceptor;

public abstract class PA implements AcceptorCommand {
    public int lambda;

    public PA(int lambda) {
        this.lambda = lambda;
    }
}