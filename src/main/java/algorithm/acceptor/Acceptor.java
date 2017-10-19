package algorithm.acceptor;

import algorithm.messages.acceptor.*;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.util.HashSet;
import java.util.Set;

class Acceptor {

    private int ballotNumber = -1;
    private final Set<BallotProposal> accepts = new HashSet<>();

    Acceptor(Server server, int id) {
        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object o) {
                if (o instanceof P1A) {
                    P1A p1A = (P1A) o;
                    System.out.println("I got p1a " + p1A);
                    if (p1A.ballot > ballotNumber) {
                        ballotNumber = p1A.ballot;
                        System.out.println("my new ballot number is " + ballotNumber);
                    }
                    connection.sendTCP(new P1B(ballotNumber, accepts, id));
                } else if (o instanceof P2A) {
                    P2A p2A = (P2A) o;
                    System.out.println("I got p2a " + p2A);
                    if (p2A.ballotProposal.ballot == ballotNumber) {
                        accepts.add(p2A.ballotProposal);
                    }
                    connection.sendTCP(new P2B(ballotNumber, id, p2A.commanderId));
                }
            }
        });
    }
}
