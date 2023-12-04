import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import org.example.JSONReader;


import java.util.ArrayList;
import java.util.List;

public class DataStorage{

    JSONReader reader;

    List<ShoppingList> lists;
    List<User> users;

    public DataStorage(){

        this.reader = new JSONReader();
        this.lists = new ArrayList<>();
        this.users = new ArrayList<>();

        for (JSONObject jsonObject : reader.getFiles()) {
            processJSONObject(jsonObject);
        }

    }

    public String getUser(){
        return users.get(1).getUsername();
    }

    private void processJSONObject(JSONObject jsonObject) {
        for (Object userKey : jsonObject.keySet()) {
            
            User newUser = new User(userKey.toString());
            users.add(newUser);

            JSONObject userObject = (JSONObject) jsonObject.get(userKey);

            JSONArray shoppingListArray = (JSONArray) userObject.get("shopping_lists");

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

                lists.add(newShoppingList);
            }
        }
    }






}