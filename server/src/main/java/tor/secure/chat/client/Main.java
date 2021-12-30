package tor.secure.chat.client;

import java.util.Scanner;

public class Main {
    
    public static void main(String[] _args) {
        Client client = new Client("localhost", 6666);

        client.start();

        try (var scanner = new Scanner(System.in)) {
            while (true) {
                String[] args = scanner.nextLine().split(" ");

                switch (args[0]) {
                    case "register" -> client.register(args[1], args[2]);
                    case "login" -> client.login(args[1], args[2]);
                    case "send" -> client.sendMessage(args[1], args[2]);
                }
            }
        }
    }

}
