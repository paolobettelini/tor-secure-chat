package ch.bettelini.server;

import java.util.Scanner;

public class Main {

    /**
     * 
     * TODO
     * Limitare il numero di messaggi
     * Pacchetti per richiedere messaggi più vecchi
     * 
     * database dovrebbe essere in server/ e non protocol/
     */

    public static void main(String[] args) {
        Server server = new Server(6666);
        server.start();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String cmd = scanner.nextLine();

                String res = switch (cmd) {
                    case "ls" -> {
                        StringBuilder builder = new StringBuilder();

                        server.getUsers().forEach(builder::append);

                        yield builder.toString();
                    }
                    default -> """
                        Help:
                        ls\tprint all the connected users
                        stop\tshutdown the server
                        help\tdisplay this message
                    """;
                };

                System.out.println(res);
            }
        }
    }

}