package com.shopup;

import java.io.IOException;
import java.util.List;

import static com.shopup.JSONHandler.readFromJSON;

public class Main {
    public static void main(String[] args) throws IOException {
        User user2 = readFromJSON("emily_clark.json", false);
        User user1 = readFromJSON("john_doe.json", false);

        System.out.println(user1.getUsername());
        for (ShoppingList list : user1.getShoppingListList()){
            System.out.println(list.getName() + ":");

            for(Product product : list.getProductList()){
                System.out.println(product.getName() + " - " + product.getQuantity());
            }
            System.out.println("\n");
        }

        System.out.println(user2.getUsername());
        for (ShoppingList list : user2.getShoppingListList()){
            System.out.println(list.getName() + ":");

            for(Product product : list.getProductList()){
                System.out.println(product.getName() + " - " + product.getQuantity());
            }
            System.out.println("\n");
        }

        System.out.println("VOU DAR PRINT");

        user1.getShoppingListList().get(0).getProductList().get(0).incrementQuantity(user1.getId(),1);
        user2.getShoppingListList().get(0).getProductList().get(1).decrementQuantity(user2.getId(),1);

        for(Product product : user1.getShoppingListList().get(0).getProductList()){
            System.out.println(product.getName() + " - " + product.getQuantity());
        }

        for(Product product : user2.getShoppingListList().get(0).getProductList()){
            System.out.println(product.getName() + " - " + product.getQuantity());
        }
/*
        user1.getShoppingListList().get(0).getProductList().get(0).merge(user2.getShoppingListList().get(0).getProductList().get(0));
        user1.getShoppingListList().get(0).getProductList().get(1).merge(user2.getShoppingListList().get(0).getProductList().get(1));

        System.out.println("VOU DAR PRINT E REZAR QUE TENHA DADO MERGE");

        System.out.println(user1.getUsername());

        for(Product product : user1.getShoppingListList().get(0).getProductList()){
            System.out.println(product.getName() + " - " + product.getQuantity());
        }
        System.out.println("\n");

        System.out.println(user2.getUsername());
        for(Product product : user2.getShoppingListList().get(0).getProductList()){
            System.out.println(product.getName() + " - " + product.getQuantity());
        }
        System.out.println("\n");*/


/*        System.out.println("\n" + user2.getUsername());
        for (ShoppingList list : user2.getShoppingListList()){
            System.out.println(list.getName() + ":");

            for(Product product : list.getProductList()){
                System.out.println(product.getName() + " - " + product.getQuantity());
            }
            System.out.println("\n");
        }*/

        user1.getShoppingListList().get(0).merge(user2.getShoppingListList().get(0),user2.getId());
        System.out.print("\nSUPOSTAMENTE ESTÁ MERGED\n\n");
        System.out.println(user1.getUsername());
        for (ShoppingList list : user1.getShoppingListList()){
            System.out.println(list.getName() + ":");

            for(Product product : list.getProductList()){
                System.out.println(product.getName() + " - " + product.getQuantity());
            }
            System.out.println("\n");
        }

        System.out.println(user2.getUsername());
        for (ShoppingList list : user2.getShoppingListList()){
            System.out.println(list.getName() + ":");

            for(Product product : list.getProductList()){
                System.out.println(product.getName() + " - " + product.getQuantity());
            }
            System.out.println("\n");
        }






    }

}
