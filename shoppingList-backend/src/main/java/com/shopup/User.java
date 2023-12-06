package com.shopup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class User {
    String username;
    UUID id;
    HashMap<UUID, ShoppingList> shoppingLists;
    PNCounter counter;

    public User(String username) {
        this.username = username;
        this.id = UUID.randomUUID();
        this.counter = new PNCounter();
        this.shoppingLists = new HashMap<>();
    }

    public User(String username, UUID id, HashMap<UUID, ShoppingList> shoppingLists) {
        this.username = username;
        this.id = UUID.randomUUID();
        this.counter = new PNCounter();
        this.shoppingLists = shoppingLists == null ? new HashMap<>() : shoppingLists;
    }

    public User(String username, UUID id) {
        this.username = username;
        this.id = id;
        this.counter = new PNCounter();
        this.shoppingLists = new HashMap<>();
    }

    @JsonCreator
    public User(@JsonProperty("username") String username,
                @JsonProperty("id") UUID id,
                @JsonProperty("shoppingLists") List<ShoppingList> shoppingListArray) {
        this.username = username;
        this.id = id != null ? id : UUID.randomUUID();
        this.counter = new PNCounter();
        this.shoppingLists = new HashMap<>();
        if (shoppingListArray != null) {
            for (ShoppingList list : shoppingListArray) {
                this.shoppingLists.put(list.getId(), list);
            }
        }
    }

    public String getUsername() {
        return this.username;
    }

    public UUID getId() {
        return this.id;
    }

    public PNCounter getCounter() {
        return this.counter;
    }

    public HashMap<UUID, ShoppingList> getShoppingLists() {
        return this.shoppingLists;
    }

    @JsonProperty("shoppingLists")
    public List<ShoppingList> getShoppingListList() {
        return new ArrayList<>(this.shoppingLists.values());
    }

    //setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPNCounter(PNCounter counter) {
        this.counter = counter;
    }
}
