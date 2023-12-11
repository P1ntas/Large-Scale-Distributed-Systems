package com.shopup;

import java.io.IOException;
import java.util.List;

import static com.shopup.JSONHandler.readFromJSON;

public class Main {
    public static void main(String[] args) throws IOException {


        TerminalInterface terminalInterface = new TerminalInterface();
        terminalInterface.startInterface();


/*        User user2 = readFromJSON("emily_clark.json", false);
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
        user2.getShoppingListList().get(0).getProductList().get(1).decrementQuantity(user2.getId(),9);
        user1.getShoppingListList().get(0).addProduct(new Product("Bananas",user1.getId(),3));
        user2.getShoppingListList().get(0).addProduct(new Product("Coke",user2.getId(),1));


        for(Product product : user1.getShoppingListList().get(0).getProductList()){
            System.out.println(product.getName() + " - " + product.getQuantity());
        }

        for(Product product : user2.getShoppingListList().get(0).getProductList()){
            System.out.println(product.getName() + " - " + product.getQuantity());
        }


        System.out.println("\nVAI COMEÇAR O MERGE\n");
        user1.getShoppingListList().get(0).merge(user2.getShoppingListList().get(0));
        System.out.println("\n\n\n\n\n\nNOMEEEEEE: " + user2.getShoppingListList().get(0).getProductList().get(2).getName());
        user2.getShoppingListList().get(0).getProductList().get(2).incrementQuantity(user2.getId(),1);
        user1.getShoppingListList().get(0).merge(user2.getShoppingListList().get(0));

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






        /*user1.getShoppingListList().get(0).merge(user2.getShoppingListList().get(0),user2.getId());
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
            System.out.println("\n");*/
        }






}


