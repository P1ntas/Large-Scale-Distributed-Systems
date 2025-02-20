package com.shopup;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import java.time.LocalTime;

import static com.shopup.JSONHandler.*;

public class TerminalInterface {

    private BufferedReader reader;
    private User currentUser;
    private boolean connectToServer;
    private Thread serverListenerThread;
    private ZContext context;
    private ZMQ.Socket socket;

    public TerminalInterface() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    private void loadUser() throws IOException {
        System.out.println("Enter your username:");
        String username = reader.readLine();
        File userFile = new File("src/main/resources/" + username + ".json");

        if (userFile.exists()) {
            this.currentUser = JSONHandler.readFromJSON(username + ".json", false);
            System.out.println("User Loaded: " + currentUser.getUsername());
        } else {
            this.currentUser = new User(username);
            writeToJSON(currentUser, false);
            System.out.println("New user created: " + currentUser.getUsername());
        }
    }

    private void connectToServer() throws IOException {
        System.out.println("Would you like to connect to the server? (yes/no)");
        String serverResponse = reader.readLine();
        this.connectToServer = serverResponse.equalsIgnoreCase("yes");

        // Implement server connection logic here if connectToServer is true
    }

    private void displayShoppingLists() {
        System.out.println("Your Shopping Lists:");
        currentUser.getShoppingLists().forEach((id, list) -> {
            System.out.println(id + ": " + list.getName());
        });
    }

    private void printShoppingList(ShoppingList list){
        System.out.println("Products in " + list.getName() + ":");
        list.getProducts().forEach((id, product) -> {
            if(product.getQuantity() != 0)
                System.out.println("ID: " + id + ", Name: " + product.getName() + ", Quantity: " + product.getQuantity());
        });
    }

    private ShoppingList addShoppingList() throws IOException {
        System.out.println("Enter name for new Shopping List:");
        String name = reader.readLine();
        ShoppingList newList = new ShoppingList(name);
        currentUser.getShoppingLists().put(newList.getId(), newList);
        System.out.println("Shopping List added: " + newList.getName());

        return newList;
    }

    private ShoppingList editShoppingList() throws IOException {
        System.out.println("Enter ID of Shopping List to edit:");
        UUID id = UUID.fromString(reader.readLine());
        ShoppingList list = currentUser.getShoppingLists().get(id);

        if (list != null) {
            System.out.println("Enter new name for Shopping List:");
            String newName = reader.readLine();
            list.setName(newName);
            System.out.println("Shopping List updated: " + list.getName());
        } else {
            System.out.println("Shopping List not found.");
        }

        return list;
    }

    private ShoppingList deleteShoppingList() throws IOException {
        System.out.println("Enter ID of Shopping List to delete:");
        UUID id = UUID.fromString(reader.readLine());
        ShoppingList list = currentUser.getShoppingLists().get(id);
        if (currentUser.getShoppingLists().remove(id) != null) {
            System.out.println("Shopping List deleted.");
        } else {
            System.out.println("Shopping List not found.");
        }
        return list;
    }

    private ShoppingList addProductToShoppingList() throws IOException {

        displayShoppingLists();
        System.out.println("Enter ID of Shopping List to add product:");
        UUID listId = UUID.fromString(reader.readLine());
        ShoppingList list = currentUser.getShoppingLists().get(listId);

        if (list != null) {
            System.out.println("Enter name for new Product:");
            String productName = reader.readLine();
            System.out.println("Enter the quantity for the product:");
            int quantity = Integer.parseInt(reader.readLine());

            //In case the product exists but quantity equals 0
            for(Product product : list.getProducts().values()){
                if(Objects.equals(product.getName(), productName)){
                    product.incrementQuantity(currentUser.getId(),quantity);
                    return list;
                }
            }

            Product newProduct = new Product(productName, currentUser.getId(), quantity);
            list.addProduct(newProduct);
            System.out.println("Product added: " + newProduct.getName());
        } else {
            System.out.println("Shopping List not found.");
        }
        return list;
    }

    private ShoppingList editProductInShoppingList() throws IOException {
        displayShoppingLists();
        System.out.println("Enter ID of Shopping List:");
        UUID listId = UUID.fromString(reader.readLine());
        ShoppingList list = currentUser.getShoppingLists().get(listId);

        if (list != null) {

            printShoppingList(list);

            System.out.println("Enter ID of Product to edit:");
            UUID productId = UUID.fromString(reader.readLine());
            Product product = list.getProducts().get(productId);

            if (product != null) {
                System.out.println("1. Change product name\n2. Change product value");
                int changeInput = Integer.parseInt(reader.readLine());

                while(changeInput < 1 || changeInput > 2){
                    System.out.println("Please input a valid number");
                    changeInput = Integer.parseInt(reader.readLine());
                }

                switch (changeInput){
                    case 1:
                        System.out.println("Enter new name for Product:");
                        String newName = reader.readLine();
                        product.setName(newName);
                    case 2:
                        System.out.println("Enter new quantity for Product:");
                        int newQuantity = Integer.parseInt(reader.readLine());

                        product.setQuantity(currentUser.getId(),newQuantity);
                }

                System.out.println("Product updated: " + product.getName());
            } else {
                System.out.println("Product not found.");
            }
        } else {
            System.out.println("Shopping List not found.");
        }
        return list;
    }

    private ShoppingList deleteProductFromShoppingList() throws IOException {
        displayShoppingLists();
        System.out.println("Enter ID of Shopping List:");
        UUID listId = UUID.fromString(reader.readLine());
        ShoppingList list = currentUser.getShoppingLists().get(listId);

        if (list != null) {
            printShoppingList(list);

            System.out.println("Enter ID of Product to delete:");
            UUID productId = UUID.fromString(reader.readLine());
            if (list.getProducts().containsKey(productId)) {
                list.getProducts().get(productId).removeProduct(currentUser.getId());
                System.out.println("Product deleted.");
            } else {
                System.out.println("Product not found.");
            }
        } else {
            System.out.println("Shopping List not found.");
        }
        return list;
    }

    private void viewProductsInShoppingList() throws IOException {
        displayShoppingLists();
        System.out.println("Enter ID of Shopping List:");
        UUID listId = UUID.fromString(reader.readLine());
        ShoppingList list = currentUser.getShoppingLists().get(listId);

        if (list != null) {
            printShoppingList(list);
        } else {
            System.out.println("Shopping List not found.");
        }
    }

    private void addExistingShoppingListToUser() throws IOException {
        System.out.println("Enter ID of the existing Shopping List to add:");
        UUID listId = UUID.fromString(reader.readLine());

        ShoppingList newList = sendListToServer(listId, this.currentUser.getUsername());

        if(newList == null){
            System.out.println("Requested list not stored in the cloud.");
        }
        else{
            this.currentUser.getShoppingLists().put(newList.getId(), newList);
            System.out.println("New list (" + newList.getName() + ") added. " + this.currentUser.getShoppingLists());
        }
        //need to receive shopping list to add to local files
    }

    private void startServerListener() {
        serverListenerThread = new Thread(() -> {
            try {
                context = new ZContext();
                socket = context.createSocket(ZMQ.SUB);
                socket.connect("tcp://127.0.0.1:5001");
                socket.subscribe("LIST_DATA".getBytes(ZMQ.CHARSET));

                while (!Thread.currentThread().isInterrupted()) {
                    String topic = socket.recvStr();
                    if (topic.equals("LIST_UPDATE")) {
                        String jsonData = socket.recvStr();
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            ShoppingList shoppingList = mapper.readValue(jsonData, ShoppingList.class);
                            currentUser.getShoppingLists().put(shoppingList.getId(), shoppingList);
                            JSONHandler.writeToJSON(currentUser, false);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    socket.close();
                }
                if (context != null) {
                    context.close();
                }
            }
        });

        serverListenerThread.start();
    }

    private void stopServerListener() {
        if (serverListenerThread != null) {
            serverListenerThread.interrupt();
            serverListenerThread = null;
        }
    }

    private void toggleServerConnection() throws IOException {
        System.out.println("Would you like to change the server connection setting (yes)? (current: " + (connectToServer ? "connected" : "disconnected") + ")");
        String response = reader.readLine();
        if (response.equalsIgnoreCase("yes")) {
            connectToServer = !connectToServer;
            System.out.println("Server connection is now " + (connectToServer ? "enabled" : "disabled"));
        }
    }

    public void startInterface() {
        try {
            loadUser();

            while (true) {
                System.out.println("\nChoose an action: \n1. Display Shopping Lists \n2. Add Shopping List \n3. Edit Shopping List name \n4. Delete Shopping List \n5. Add Product to Shopping List \n6. Edit Product in Shopping List \n7. Delete Product from Shopping List \n8. View Products in Shopping List \n9. Add Existing Shopping List to User \n10 Toggle Server Connection \n11. Exit");
                String choice = reader.readLine();
                ShoppingList list;

                switch (choice) {
                    case "1" -> displayShoppingLists();
                    case "2" -> {
                        list = addShoppingList();
                        writeToJSON(currentUser, false);
                        if (this.connectToServer){
                            ShoppingList newList = sendListToServer(list, this.currentUser.getUsername());
                            if(newList!=null) this.currentUser.getShoppingLists().put(newList.getId(), newList);
                        }
                    }
                    case "3" -> {
                        list = editShoppingList();
                        writeToJSON(currentUser, false);
                        if (this.connectToServer){
                            ShoppingList newList = sendListToServer(list, this.currentUser.getUsername());
                            if(newList!=null) this.currentUser.getShoppingLists().put(newList.getId(), newList);
                        }
                    }
                    case "4" -> {
                        list = deleteShoppingList();
                        writeToJSON(currentUser, false);
                        if (this.connectToServer){
                            sendDeleteListToServer(list, this.currentUser.getUsername());
                        }
                    }
                    case "5" -> {
                        list = addProductToShoppingList();
                        writeToJSON(currentUser, false);
                        if (this.connectToServer){
                            ShoppingList newList = sendListToServer(list, this.currentUser.getUsername());
                            if(newList!=null) this.currentUser.getShoppingLists().put(newList.getId(), newList);
                        }
                    }
                    case "6" -> {
                        list = editProductInShoppingList();
                        writeToJSON(currentUser, false);
                        if (this.connectToServer){
                            ShoppingList newList = sendListToServer(list, this.currentUser.getUsername());
                            if(newList!=null) this.currentUser.getShoppingLists().put(newList.getId(), newList);
                        }
                    }
                    case "7" -> {
                        list = deleteProductFromShoppingList();
                        writeToJSON(currentUser, false);
                        if (this.connectToServer){
                            ShoppingList newList = sendListToServer(list, this.currentUser.getUsername());
                            if(newList!=null) this.currentUser.getShoppingLists().put(newList.getId(), newList);
                        }
                    }
                    case "8" -> viewProductsInShoppingList();
                    case "9" -> {
                        if (!this.connectToServer) {
                            System.out.println("Please establish connection first.\n");
                        }
                        else{
                            addExistingShoppingListToUser();
                        }
                    }
                    case "10" -> toggleServerConnection();
                    case "11" -> {
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            }

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        TerminalInterface ti = new TerminalInterface();
        ti.startInterface();
    }
}
