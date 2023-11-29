package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class JSONReader {

    JSONParser parser;
    FileReader reader;
    JSONObject jsonObject;

    public JSONReader(String filepath){

        try{

        this.reader = new FileReader(filepath);
        this.parser = new JSONParser();

        this.jsonObject = (JSONObject) parser.parse(reader);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void info(){
        for (Object key : jsonObject.keySet()) {
            System.out.println("-------------------------------------------------------------------------------------------------");
            String username = (String) key;
            JSONObject userObject = (JSONObject) jsonObject.get(username);

            System.out.println("User: " + username);

            JSONArray shoppingLists = (JSONArray) userObject.get("shopping_lists");
            for (Object listObj : shoppingLists) {
                JSONObject list = (JSONObject) listObj;

                String listName = (String) list.get("list_name");
                String lastEdited = (String) list.get("last_edited");
                JSONArray products = (JSONArray) list.get("products");

                System.out.println("\tShopping List: " + listName);
                System.out.println("\tLast Edited: " + lastEdited);

                for (Object productObj : products) {
                    JSONObject product = (JSONObject) productObj;

                    String productName = (String) product.get("name");
                    long quantity = (long) product.get("quantity");

                    System.out.println("\t\tProduct: " + productName + ", Quantity: " + quantity);
                }
            }
            System.out.println("-------------------------------------------------------------------------------------------------");
            System.out.println();
        }    
    }


    /*
    public static void main(String[] args) {
        JSONParser parser = new JSONParser();

        try {
            // Path to your JSON file
            FileReader reader = new FileReader("path/to/your/file.json");

            // Parse the JSON file
            Object obj = parser.parse(reader);

            // Cast the parsed object to JSONObject
            JSONObject jsonObject = (JSONObject) obj;

            // Iterate over each user in the JSON
            for (Object key : jsonObject.keySet()) {
                String username = (String) key;
                JSONObject userObject = (JSONObject) jsonObject.get(username);

                System.out.println("User: " + username);

                // Iterate over shopping lists for each user
                JSONArray shoppingLists = (JSONArray) userObject.get("shopping_lists");
                for (Object listObj : shoppingLists) {
                    JSONObject list = (JSONObject) listObj;

                    String listName = (String) list.get("list_name");
                    String lastEdited = (String) list.get("last_edited");
                    JSONArray products = (JSONArray) list.get("products");

                    System.out.println("\tShopping List: " + listName);
                    System.out.println("\tLast Edited: " + lastEdited);

                    // Iterate over products in the shopping list
                    for (Object productObj : products) {
                        JSONObject product = (JSONObject) productObj;

                        String productName = (String) product.get("name");
                        long quantity = (long) product.get("quantity");

                        System.out.println("\t\tProduct: " + productName + ", Quantity: " + quantity);
                    }
                }
                System.out.println();
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
    */
}
