package clients;

import algorithm.Utils;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.util.Scanner;

public class MainClient {
    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);

        if (args == null || args.length != 2) {
            System.err.format("Usage: %s <host> <port>\n", MainClient.class.getSimpleName());
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        Client client = Utils.getClient(1000, host, port);
        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object o) {
                if (o instanceof String) {
                    System.out.println(o);
                }
            }
        });
        String command;
        while (!(command = in.next()).equals("exit")) {
            switch (command) {
                case "get": {
                    String x = in.next();
                    client.sendTCP(command + " " + x);
                    break;
                }
                case "set": {
                    String x = in.next();
                    String y = in.next();
                    client.sendTCP(command + " " + x + " " + y);
                    break;
                }
                case "delete": {
                    String x = in.next();
                    client.sendTCP(command + " " + x);
                    break;
                }
                case "ping": {
                    client.sendTCP(command);
                    break;
                }
                default:
                    System.err.println("Wrong name of command");
                    break;
            }
        }
    }
}
