package com.shopup;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {

            String serverAddress;
            String brokerAddress = "tcp://127.0.0.1:5000";

            if (args.length > 0) {
                serverAddress = args[0];
            } else {
                System.out.println("Enter a valid server address (e.g., tcp://127.0.0.2:5000): ");
                Scanner scanner = new Scanner(System.in);
                serverAddress = scanner.nextLine();
                scanner.close();
            }

            ServerNode node = new ServerNode(serverAddress, brokerAddress);
            node.start();

            // Read user data from JSON
            Map<String, User> users = JSONHandler.readFromJSON("");

            // Distribute user data across server nodes
            JSONHandler.distributeUserData(users, node.getServerAddress(), node.getRing()); // Assuming you have consistentHashing and ring instances

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
