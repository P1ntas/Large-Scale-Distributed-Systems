package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class JSONReader {

    //JSONObject jsonObject;
    List<JSONObject> files;

    public JSONReader(){


        this.files = readJSONFilesFromFolder("src/main/resources");

        //this.jsonObject = (JSONObject) parser.parse(reader);

    }

    public List<JSONObject> readJSONFilesFromFolder(String folderPath) {
        List<JSONObject> jsonObjects = new ArrayList<>();

        File folder = new File(folderPath);

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".json")) {
                        try {
                            JSONObject jsonObject = readJSONFromFile(file);
                            jsonObjects.add(jsonObject);
                        } catch (IOException | ParseException e) {
                            System.err.println("Error reading JSON file: " + file.getAbsolutePath());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return jsonObjects;
    }

    private JSONObject readJSONFromFile(File file) throws IOException, ParseException {
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(file)) {
            return (JSONObject) parser.parse(reader);
        }
    }

    public List<JSONObject> getFiles(){
        return files;
    }

    /*
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
    */

}
