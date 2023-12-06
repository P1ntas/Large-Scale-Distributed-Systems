package com.shopup;

import java.util.Scanner;

import static com.shopup.JSONHandler.*;
import static com.shopup.Utils.mergeNames;

public class TestEnvironment {
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
            User user = readFromJSON("", true);

            // Distribute user data across server nodes
            distributeUserData(user, node.getServerAddress(), node.getRing()); // Assuming you have consistentHashing and ring instances
            node.loadAndMergeUsers();

            writeToJSON(user, false);

        } catch (Exception e) {
            e.printStackTrace();
        }
        /*String res = mergeNames("banana2", "banana1");
        System.out.println(res);*/
    }
}
