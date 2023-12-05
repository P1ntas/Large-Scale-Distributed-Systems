package org.crdt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JSONHandler {
    private static final String DEFAULT_FILE_NAME = "src/main/resources/test.json";

    public static Map<String, User> readFromJSON(String fileName) throws IOException {
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
    }

    public static void writeToJSON(User user) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String fileName = "src/main/resources/" + user.getUsername() + ".json";
        mapper.writeValue(new File(fileName), user);
    }

    // Example usage
    public static void main(String[] args) {
        try {
            Map<String, User> users = readFromJSON("");
            if (!users.isEmpty()) {
                User user = users.values().iterator().next(); // Getting the first user
                writeToJSON(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
