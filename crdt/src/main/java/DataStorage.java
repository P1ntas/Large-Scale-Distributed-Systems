import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import org.example.JSONReader;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.FileWriter;
import java.io.IOException;

import java.util.UUID;

public class DataStorage{

    JSONReader reader;

    Map<User, List<ShoppingList>> user_lists;

    public DataStorage(){

        this.reader = new JSONReader();
        this.user_lists = new HashMap<>();

        for (JSONObject jsonObject : reader.getFiles()) {
            processJSONObject(jsonObject);
        }

    }

    public String getUser(){
        return user_lists.keySet().iterator().next().getUsername();
    }

    public String getShoppingList(){
        return user_lists.get(user_lists.keySet().iterator().next()).get(0).getName();
    }

    private void processJSONObject(JSONObject jsonObject) {
        for (Object userKey : jsonObject.keySet()) {
            
            User newUser = new User(userKey.toString());
            

            JSONObject userObject = (JSONObject) jsonObject.get(userKey);

            JSONArray shoppingListArray = (JSONArray) userObject.get("shopping_lists");

            List<ShoppingList> newList = new ArrayList<>(); 

            for (Object shoppingListObject : shoppingListArray) {
                JSONObject listObject = (JSONObject) shoppingListObject;

                ShoppingList newShoppingList = new ShoppingList((String) listObject.get("list_name"));    

                JSONArray productsArray = (JSONArray) listObject.get("products");

                // Tem de adicionar isto em algum canto, creio que no Vector clock, mas não tenho certeza listObject.get("last_edited")

                for (Object productObject : productsArray) {
                    JSONObject product = (JSONObject) productObject;

                    Product newProduct = new Product((String) product.get("name"));
                    newProduct.setQuantity(((Long) product.get("quantity")).intValue());

                    newShoppingList.addProduct(newProduct);

                }

                newList.add(newShoppingList);
            }

            user_lists.put(newUser, newList);

        }
        

        }
        
            
    public void convertToJSON(){

        for(User key: user_lists.keySet()){

            JSONObject user = new JSONObject();
            JSONArray shopping_lists = new JSONArray();

            for(ShoppingList list: user_lists.get(key)){

                JSONObject shoppingList = new JSONObject();
                shoppingList.put("list_name", list.getName());
                shoppingList.put("last_edited", "2023-11-12T12:00:00Z");

                JSONArray products = new JSONArray();

                HashMap<UUID, Product> prod = list.getProducts();

                for(UUID id: prod.keySet()){

                    JSONObject product = new JSONObject();
                    product.put("name", prod.get(id).getName());
                    product.put("quantity", prod.get(id).getQuantity());

                    products.add(product);

                }

                shoppingList.put("products", products);
                shopping_lists.add(shoppingList);

            }

            JSONObject finalList = new JSONObject();
            finalList.put("shopping_lists", shopping_lists);

            user.put(key.getUsername(), shopping_lists);

            try (FileWriter fileWriter = new FileWriter("src/main/resources/" + key.getId() + ".json")) {
                fileWriter.write(user.toJSONString());
                System.out.println("JSON file created successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /*private String formatJsonString(String jsonString) {
        try {
            // Parse the input JSON string and return a formatted string
            JSONObject jsonObject = (JSONObject) new org.json.simple.parser.JSONParser().parse(jsonString);
            return jsonObject.toJSONString();
        } catch (ParseException e) {
            // Handle the parsing exception (e.g., print the error)
            e.printStackTrace();
            return jsonString; // Return the original string if parsing fails
        }
    }*/
}
