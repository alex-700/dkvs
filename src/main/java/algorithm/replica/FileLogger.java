package algorithm.replica;

import algorithm.messages.replica.Command;
import algorithm.messages.replica.DeleteCommand;
import algorithm.messages.replica.SetCommand;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.Scanner;

class FileLogger {
    private final Path file;

    FileLogger(String fileName) {
        file = Paths.get(fileName);
    }

    int load(Map<String, String> data) {
        try (Scanner in = new Scanner(file)) {
            int count = 0;
            while (in.hasNext()) {
                switch (in.next()) {
                    case "SET":
                        data.put(in.next(), in.next());
                        count++;
                        break;
                    case "DELETE":
                        data.remove(in.next());
                        count++;
                        break;
                    default:
                        System.err.println("Unknown operation in log");
                }
            }
            return count;
        } catch (FileNotFoundException e) {
            System.err.println("Log file not found");
            return 0;
        } catch (IOException e) {
            System.err.println("Some io exception:\n" + e.getMessage());
            return 0;
        }
    }

    void log(Command c) {
        if (c instanceof SetCommand) {
            SetCommand s = (SetCommand) c;
            appendLineToFile(String.format("SET %s %s", s.key, s.value));
        } else if (c instanceof DeleteCommand) {
            DeleteCommand d = (DeleteCommand) c;
            appendLineToFile(String.format("DELETE %s", d.key));
        }
    }

    private void appendLineToFile(String s) {
        if (!Files.exists(file)) {
            try {
                Files.createFile(file);
            } catch (IOException e) {
                System.err.println("Can't create file");
            }
        }
        try {
            Files.write(file, (s + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Can't write");
        }
    }
}
