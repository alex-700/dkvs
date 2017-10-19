package algorithm.acceptor;

import algorithm.Utils;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.Map;

public class MainAcceptor {
    public static void main(String[] args) throws IOException {
        if (args == null || args.length != 1) {
            return;
        }
        int id = Integer.parseInt(args[0]);
        Map<String, Object> map = Utils.parseYaml("acceptors", id);
        @SuppressWarnings("null")
        int port = (int) map.get("port");
        System.out.format("acceptor %d listen on port %d\n", id, port);

        Server server = Utils.getServer(port);

        @SuppressWarnings("unused")
        Acceptor acceptor = new Acceptor(server, id);
        System.out.println("start acceptor");
    }
}
