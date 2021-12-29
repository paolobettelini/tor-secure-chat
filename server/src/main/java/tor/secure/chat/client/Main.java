package tor.secure.chat.client;

import java.util.Scanner;

public class Main {
    
    public static void main(String[] args) {
        Client client = new Client("localhost", 6666);

        client.start();

        try (var scanner = new Scanner(System.in)) {
            while (true) {
                String cmd = scanner.nextLine();

                switch (cmd) {
                    case "register" -> client.register("paolo", "SONOFROCIO123_");
                }
            }
        }
    }

}
