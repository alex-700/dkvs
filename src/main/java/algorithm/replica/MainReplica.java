package algorithm.replica;

import algorithm.Utils;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainReplica {

    public static final int TIMEOUT = 1000;

    public static void main(String[] args) {
        if (args == null || args.length != 1) {
            System.err.format("Usage: %s <id>\n", MainReplica.class.getSimpleName());
            return;
        }

        int id = Integer.parseInt(args[0]);
        Map<String, Object> map = Utils.parseYaml("replicas", id);
        int port = (int) map.get("port");
        int countReplicas = (int) map.get("countReplicas");
        Client toYourself = null;
        Server server = null;
        try {
            server = Utils.getServer(port);
            toYourself = Utils.getClient(TIMEOUT, (String) map.get("ip"), (int) map.get("port"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<Client> toLeaders = new ArrayList<>();
        Map<Integer, Map<String, Object>> leadersInfo = Utils.getOtherConfig("leaders");
        if (leadersInfo == null) {
            System.err.println("Parse config error");
            return;
        }
        for (Map.Entry<Integer, Map<String, Object>> p : leadersInfo.entrySet()){
            try {
                System.out.format("connect to leader on %s:%d\n", p.getValue().get("ip"), p.getValue().get("port"));
                toLeaders.add(Utils.getClient(TIMEOUT, (String) p.getValue().get("ip"), (int) p.getValue().get("port")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Replica replica = new Replica(toLeaders, server, id, countReplicas, toYourself);
        System.out.format("start replica %d listen on port %d\n", id, port);
    }
}