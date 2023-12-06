package com.shopup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JSONHandler {
    private static final String DEFAULT_FILE_NAME = "john_doe.json";

    /*public static Map<String, User> readFromJSON(String fileName) throws IOException {
        if (fileName == null || fileName.isEmpty()) {
            fileName = DEFAULT_FILE_NAME;
        }

        ObjectMapper mapper = new ObjectMapper();
        TypeFactory typeFactory = mapper.getTypeFactory();

        File file = new File(fileName);
        Map<String, Map<String, Object>> data = mapper.readValue(file, new TypeReference<Map<String, Map<String, Object>>>() {});

        Map<String, User> users = new HashMap<>();

        data.forEach((username, userData) -> {
            UUID userId = UUID.fromString((String) userData.get("id"));
            User user = new User(username, userId);

            @SuppressWarnings("unchecked")
            var shoppingListsData = (List<Map<String, Object>>) userData.get("shopping_lists");

            shoppingListsData.forEach(listData -> {
                String listName = (String) listData.get("list_name");
                UUID listId = UUID.fromString((String) listData.get("id"));
                ShoppingList shoppingList = new ShoppingList(listName, listId, new HashMap<>());

                @SuppressWarnings("unchecked")
                var productsData = (List<Map<String, Object>>) listData.get("products");
                productsData.forEach(productData -> {
                    String productName = (String) productData.get("name");
                    UUID productId = UUID.fromString((String) productData.get("id"));
                    int quantity = (int) productData.get("quantity");
                    String lastEditedStr = (String) productData.get("last_edited");
                    long lastEdited = Instant.parse(lastEditedStr).toEpochMilli();

                    Product product = new Product(productName, productId, quantity, lastEdited);
                    shoppingList.addProduct(product);
                });

                user.getShoppingLists().put(listId, shoppingList);
            });

            users.put(username, user);
        });

        return users;
    }*/

    public static User readFromJSON(String fileName, boolean server) throws IOException {
        if (fileName == null || fileName.isEmpty()) {
            fileName = DEFAULT_FILE_NAME;
        }

        if (server) fileName = "server/" + fileName;
        else fileName = "src/main/resources/" + fileName;

        ObjectMapper mapper = new ObjectMapper();

        File file = new File(fileName);
        User user = mapper.readValue(file, User.class);

        // Assuming User class has fields: username, id, shoppingLists, and counter
        // and these fields have proper getters/setters

        return user;
    }

    public static void writeToJSON(User user, boolean server) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String fileName = "" + user.getUsername() + ".json";

        if (server) {
            fileName = "server/" + user.getUsername() + ".json";
        }
        mapper.writeValue(new File(fileName), user);
    }

    public static void distributeUserData(User user, String serverAddress, TreeMap<Integer, String> ring) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        try (ZContext context = new ZContext()) {
            try (ZMQ.Socket socket = context.createSocket(ZMQ.REQ)) {
                socket.connect(serverAddress);
                String jsonData = mapper.writeValueAsString(user);
                ZMsg msg = new ZMsg();
                msg.addString("USER_DATA");
                msg.addString(jsonData);
                msg.send(socket);

                // Await response or confirmation, if necessary
                // ZMsg response = ZMsg.recvMsg(socket);
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
