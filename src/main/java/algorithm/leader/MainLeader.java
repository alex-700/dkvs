package algorithm.leader;

import algorithm.Utils;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.*;

public class MainLeader {
    public static final int TIMEOUT = 1000;

    public static void main(String[] args) {
        if (args == null || args.length != 1) {
            System.err.println("Usage: MainLeader <id>");
            return;
        }
        int id = Integer.parseInt(args[0]);
        Map<String, Object> map = Utils.parseYaml("leaders", id);
        if (map == null) {
            System.err.println("Parse error in config file");
            return;
        }
        int countLeaders = (int) map.get("countLeaders");
        int port = (int) map.get("port");
        Server server = null;
        Client clientToLoader = null;
        try {
            server = Utils.getServer(port);
            clientToLoader = Utils.getClient(TIMEOUT, (String) map.get("ip"), (int) map.get("port"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.format("leader %d listen on port %d\n", id, port);

        Map<Integer, Map<String, Object>> acceptorsInfo = Utils.getOtherConfig("acceptors");
        if (acceptorsInfo == null) {
            System.err.println("Parse config error");
            return;
        }
        int countAcceptors = acceptorsInfo.size();
        List<Client> toAcceptors = new ArrayList<>();
        Set<Integer> acceptors = new HashSet<>();
        for (int i = 0; i < countAcceptors; i++) {
            acceptors.add(i);
            try {
                System.out.format("connect to acceptor %s:%d\n", acceptorsInfo.get(i).get("ip"), acceptorsInfo.get(i).get("port"));
                toAcceptors.add(Utils.getClient(
                        TIMEOUT,
                        (String) acceptorsInfo.get(i).get("ip"),
                        (int) acceptorsInfo.get(i).get("port")
                ));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        Map<Integer, Map<String, Object>> replicasInfo = Utils.getOtherConfig("replicas");
        if (replicasInfo == null) {
            System.err.println("Parse config error");
            return;
        }
        int countReplicas = replicasInfo.size();
        List<Client> toReplicas = new ArrayList<>();
        for (int i = 0; i < countReplicas; i++) {
            try {
                System.out.format("connect to replica %s:%d\n", replicasInfo.get(i).get("ip"), replicasInfo.get(i).get("port"));
                toReplicas.add(Utils.getClient(
                        TIMEOUT,
                        (String) replicasInfo.get(i).get("ip"),
                        (int) replicasInfo.get(i).get("port")
                ));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        Leader leader = new Leader(id, countLeaders, server, toAcceptors, toReplicas, acceptors, clientToLoader);
        System.out.println("start leader");
    }
}