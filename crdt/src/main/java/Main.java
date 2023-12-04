//package org.example;

import org.example.Interface;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        DataStorage test = new DataStorage();
        System.out.println("Shopping List: " + test.getShoppingList());
        System.out.println("User: " + test.getUser());

        test.convertToJSON();

        //Interface gui = new Interface();
    }
}