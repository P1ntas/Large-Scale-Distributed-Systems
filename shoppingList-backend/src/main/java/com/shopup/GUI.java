package com.shopup;

import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

import java.io.File;
import java.io.IOException;

import static com.shopup.JSONHandler.distributeUserData;
import static com.shopup.JSONHandler.writeToJSON;


public class GUI{

    JSONReader reader;
    String option;

    boolean server;

    ServerNode node;


    public GUI(){
        this.reader = new JSONReader();

        askServerConnection();
        start();

    }

    public GUI(ServerNode node) {
        this.reader = new JSONReader();
        this.node = node;
        askServerConnection();
        start();

    }

    public void mainOptions(){
        System.out.println("[show] Show local users");
        System.out.println("[USERNAME] Select an user");
        System.out.println("Press any other key to quit!");
        System.out.println("Enter option:");
    }

    public void userOptions(){
        System.out.println("[show] Show local shopping lists");
        System.out.println("[add *shopping_list* *product_name* *quantity*] Add a new product to specific shopping list");
        System.out.println("[remove *shopping_list* *product_name*] Remove specific product from the shopping list or an entire shopping list");
        System.out.println("[edit *shopping_list* *product_name* *+/-* *quantity*] Increase or decrease the product quantity from shopping list");
        System.out.println("[save] Save modification");
        System.out.println("Press any other key to quit!");
        System.out.println("Enter option:");
    }

    private void askServerConnection() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Do you wish to connect to the server? (yes/no)");
        String response = scanner.nextLine().trim().toLowerCase();
        this.server = response.equals("yes");
        System.out.println("Server connection set to: " + this.server);
    }

    public void start(){
        Scanner scanner = new Scanner(System.in);

        while(true){
            mainOptions();

            option = scanner.next();
            scanner.nextLine();
            switch(option){
                case "show":
                    for(JSONObject file: reader.getFiles()){
                        System.out.println(file.get("username"));
                    }
                    break;
                default:

                    if(reader.checkUsername(option)){

                        try{
                            User user = JSONHandler.readFromJSON(option + ".json", false);
                            while(true){

                                userOptions();
                                String input = scanner.nextLine();

                                List<ShoppingList> lists = user.getShoppingListList();
                                String[] parts = input.split("\\s+");

                                option = parts[0];
                                System.out.println(option);

                                switch(option){
                                    case "show":

                                        for(ShoppingList list: lists){
                                            List<Product> products = list.getProductList();

                                            System.out.println("Shopping List Name: " + list.getName() );

                                            for(Product product: products){
                                                System.out.println("Product: " + product.getName() + "\t Quantity: " + product.getQuantity() );
                                            }
                                        }

                                        break;

                                    case "add":

                                        for(ShoppingList list: lists){
                                            if((parts[1].equals(list.getName()))){
                                                for(Product product: list.getProductList()){

                                                    if((parts[2].equals(product.getName()))){
                                                        System.out.println("Already exists!");
                                                        break;
                                                    }

                                                }
                                                Product newProduct = new Product(parts[2]);
                                                newProduct.setQuantity(Integer.parseInt(parts[3]));
                                                user.updateShoppingList(list.getId(), newProduct.getId(), newProduct);
                                                break;
                                            }
                                        }
                                        break;

                                    case "remove":


                                        for(ShoppingList list: lists){
                                            if((parts[1].equals(list.getName())) && parts.length == 2){
                                                user.removeShoppingList(list.getId());
                                                break;
                                            }
                                            if((parts[1].equals(list.getName())) && parts.length == 3){
                                                for(Product product: list.getProductList()){
                                                    if((parts[2].equals(product.getName()))){

                                                        user.removeProduct(list.getId(), product.getId());
                                                        break;
                                                    }
                                                }
                                            }

                                        }
                                        break;

                                    case "edit":
                                        System.out.println(parts[4]);
                                        for(ShoppingList list: lists){
                                            for(Product product: list.getProductList()){
                                                if((parts[1].equals(list.getName())) && (parts[2].equals(product.getName()))){
                                                    switch(parts[3]){
                                                        case "+":

                                                            //product.incrementQuantity(Integer.parseInt(parts[4]));
                                                            break;
                                                        case "-":
                                                            //product.decrementQuantity(Integer.parseInt(parts[4]));
                                                            break;

                                                    }
                                                    user.updateShoppingList(list.getId(), product.getId(), product);
                                                }
                                            }
                                        }
                                        break;
                                    case "save":

                                        writeToJSON(user, false);
                                        //if (this.server)  distributeUserData(user, this.node.getServerAddress(), this.node.getRing());
                                        System.out.println("Saved!");
                                        option = "quit";
                                        break;

                                    default:
                                        if(option == "quit"){
                                            scanner.close();
                                            break;
                                        }
                                        System.out.println("Invalid option, try again:");
                                        break;

                                }
                                if(option == "quit"){
                                    break;
                                }
                            }
                        } catch (IOException e){
                            System.out.println("IOException caught " + e.getMessage());
                        }

                    }else{
                        if(option == "quit"){
                            scanner.close();
                            break;
                        }

                        System.out.println("Invalid option, try again:");
                        break;
                    }
            }

            if(option == "quit"){
                break;
            }





        }
    }


    public static void main(String[] args) {

        GUI binterface = new GUI();
        binterface.start();
    }

}
