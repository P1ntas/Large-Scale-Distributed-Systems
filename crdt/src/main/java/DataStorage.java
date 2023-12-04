import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import org.example.JSONReader;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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






}