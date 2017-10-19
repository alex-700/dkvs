package algorithm.messages.acceptor;

abstract class PA implements AcceptorCommand {
    int lambda;

    PA(int lambda) {
        this.lambda = lambda;
    }
}
