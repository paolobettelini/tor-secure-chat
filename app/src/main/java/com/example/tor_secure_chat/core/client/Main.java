package com.example.tor_secure_chat.core.client;

public class Main {
    
    /*public static void main(String[] _args) throws Exception {
        Client client = new Client("localhost", 6666) {

            @Override
            void onError(int statusCode) {
                System.out.println("status code: " + statusCode);
            }

            @Override
            void onMessage(String sender, String message, long timestamp) {
                System.out.println(Instant.ofEpochMilli(timestamp) + " [" + sender + "] " + message);
            }
            
        };

        client.start();

        try (var scanner = new Scanner(System.in)) {
            while (true) {
                String[] args = scanner.nextLine().split(" ");

                switch (args[0]) {
                    case "register" -> client.register(args[1], args[2]);
                    case "login" -> client.login(args[1], args[2]);
                    case "send" -> client.sendMessage(args[1], args[2]);
                    case "fingerprint" -> System.out.println(client.getChatFingerprint(args[1]).get());
                    case "pub" -> client.retrievePublicKey(args[1]);
                }
            }
        }
    }*/

}
