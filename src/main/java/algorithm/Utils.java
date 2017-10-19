package algorithm;

import algorithm.acceptor.BallotProposal;
import algorithm.messages.acceptor.P1A;
import algorithm.messages.acceptor.P1B;
import algorithm.messages.acceptor.P2A;
import algorithm.messages.acceptor.P2B;
import algorithm.messages.leader.AdoptedCommand;
import algorithm.messages.leader.LeaderCommand;
import algorithm.messages.leader.PreemptedCommand;
import algorithm.messages.replica.*;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import static java.lang.ClassLoader.getSystemClassLoader;

public class Utils {

    private static void registerClasses(Kryo kryo) {
        kryo.setRegistrationRequired(true);
        kryo.register(P1A.class);
        kryo.register(P2A.class);
        kryo.register(P1B.class);
        kryo.register(P2B.class);
        kryo.register(BallotProposal.class);
        kryo.register(HashSet.class);
        kryo.register(LeaderCommand.class);
        kryo.register(AdoptedCommand.class);
        kryo.register(PreemptedCommand.class);
        kryo.register(Propose.class);
        kryo.register(Decision.class);
        kryo.register(Command.class);
        kryo.register(SetCommand.class);
        kryo.register(DeleteCommand.class);
    }

    public static Server getServer(int port) throws IOException {
        Server server = new Server();
        registerClasses(server.getKryo());
        server.start();
        server.bind(port);
        return server;
    }

    public static Client getClient(int timeout, String host, int port) throws IOException {
        Client client = new Client();
        registerClasses(client.getKryo());
        client.start();
        for (int i = 0; i < 6; i++) {
            try {
                client.connect(timeout, host, port);
                break;
            } catch (IOException e) {
                if (i == 5) {
                    throw e;
                }
            }
        }
        return client;
    }


    private static final String fileName = "dkvs.yaml";

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public static Map<Integer, Map<String, Object>> getOtherConfig(String type) {
        try {
            return ((Map<String, Map<Integer, Map<String, Object>>>) new Yaml().load(new FileInputStream(
                    getSystemClassLoader().getResource(fileName).getFile()))).get(type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public static Map<String, Object> parseYaml(String type, int id)  {
        try {
            Map<String, Map<Integer, Map<String, Object>>> map = (Map<String, Map<Integer, Map<String, Object>>>) new Yaml().load(new FileInputStream(
                   getSystemClassLoader().getResource(fileName).getFile()));
            return map.get(type).get(id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void send(Client client, Object message) {
        if (!client.isConnected()) {
            System.err.println("Reconnecting...");
            try {
                client.reconnect();
            } catch (IOException e) {
                System.err.println("Fail reconnecting");
                return;
            }
        }
        client.sendTCP(message);
    }

}
