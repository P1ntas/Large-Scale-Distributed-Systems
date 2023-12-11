package com.shopup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.sun.tools.jconsole.JConsoleContext;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JSONHandler {
    private static final String DEFAULT_FILE_NAME = "john_doe.json";

    public static User readFromJSON(String fileName, boolean server) throws IOException {
        if (fileName == null || fileName.isEmpty()) {
            fileName = DEFAULT_FILE_NAME;
        }

        if (server) fileName = "server/" + fileName;
        else fileName = "src/main/resources/" + fileName;

        ObjectMapper mapper = new ObjectMapper();

        File file = new File(fileName);
        User user = mapper.readValue(file, User.class);

        return user;
    }

    public static void writeToJSON(User user, boolean server) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String fileName = "src/main/resources/" + user.getUsername() + ".json";

        if (server) {
            fileName = "server/" + user.getUsername() + ".json";
        }
        mapper.writeValue(new File(fileName), user);


    }

    public static ShoppingList sendListToServer(ShoppingList shoppingList, String identity) throws JsonProcessingException {
        ShoppingList newShoppingList = null;
        ObjectMapper mapper = new ObjectMapper();
        try (ZContext context = new ZContext()) {
            try (ZMQ.Socket socket = context.createSocket(SocketType.REQ)) {

                socket.setIdentity(identity.getBytes());

                socket.connect("tcp://127.0.0.1:5001");
                String jsonData = mapper.writeValueAsString(shoppingList);

                ZMsg msg = new ZMsg();

                msg.addString("UPDATE");
                msg.addString(jsonData);
                msg.send(socket);

                System.out.println("Waiting for response...\n");
                ZMsg response = ZMsg.recvMsg(socket);

                System.out.println("RECEIVED LIST: " + response + "\n");
                String responseJSON = response.popString();
                socket.disconnect("tcp://127.0.0.1:5001");
                socket.close();
                try {
                    newShoppingList = mapper.readValue(responseJSON, ShoppingList.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return newShoppingList;
    }

    public static ShoppingList sendListToServer(UUID uuid, String identity) {
        ShoppingList newShoppingList = null;
        ObjectMapper mapper = new ObjectMapper();

        try (ZContext context = new ZContext()) {
            try (ZMQ.Socket socket = context.createSocket(SocketType.REQ)) {
                socket.setIdentity(identity.getBytes());

                socket.connect("tcp://127.0.0.1:5001");
                ZMsg msg = new ZMsg();

                msg.addString("REQUEST");
                msg.addString(uuid.toString());
                System.out.println("MESSAGE: " + msg + "\n");
                msg.send(socket);

                ZMsg response = ZMsg.recvMsg(socket);
                System.out.println("RECEIVED LIST: " + response + "\n");
                String responseJSON = response.popString();
                socket.disconnect("tcp://127.0.0.1:5001");
                socket.close();
                if (!responseJSON.equals("NULL")) {
                    try {
                        newShoppingList = mapper.readValue(responseJSON, ShoppingList.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return newShoppingList;
    }

    public static void sendDeleteListToServer(ShoppingList shoppingList, String identity) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        try (ZContext context = new ZContext()) {
            try (ZMQ.Socket socket = context.createSocket(SocketType.REQ)) {
                socket.connect("tcp://127.0.0.1:5001");
                String jsonData = mapper.writeValueAsString(shoppingList);
                ZMsg msg = new ZMsg();
                socket.setIdentity(identity.getBytes());
                msg.addString("DELETE_LIST");
                msg.addString(jsonData);
                msg.send(socket);
                socket.disconnect("tcp://127.0.0.1:5001");
                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Example usage
    public static void main(String[] args) {
        try {
            User user = readFromJSON("", false);
            User user1 = readFromJSON("emily_clark.json", false);
            writeToJSON(user, false);
            writeToJSON(user1,false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
