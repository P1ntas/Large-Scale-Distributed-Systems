package com.shopup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

import static com.shopup.Utils.mergeNames;

public class User {
    String username;
    UUID id;
    HashMap<UUID, ShoppingList> shoppingLists;

    public User(String username) {
        this.username = username;
        this.id = UUID.randomUUID();
        this.shoppingLists = new HashMap<>();
    }

    public User(String username, UUID id, HashMap<UUID, ShoppingList> shoppingLists) {
        this.username = username;
        this.id = UUID.randomUUID();
        this.shoppingLists = shoppingLists == null ? new HashMap<>() : shoppingLists;
    }

    public User(String username, UUID id) {
        this.username = username;
        this.id = id;
        this.shoppingLists = new HashMap<>();
    }

    @JsonCreator
    public User(@JsonProperty("username") String username,
                @JsonProperty("id") UUID id,
                @JsonProperty("shoppingLists") List<ShoppingList> shoppingListArray) {

        this.username = username;
        this.id = id != null ? id : UUID.randomUUID();
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


    public boolean is(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(getUsername(), user.getUsername()) && Objects.equals(getId(), user.getId()) && Objects.equals(getShoppingLists(), user.getShoppingLists());
    }

    public User merge(User other) {
/*        if (this.equals(other)) return this;
        this.username = mergeNames(this.username, other.username);

        for (ShoppingList list : this.shoppingLists.values()) {
            if (other.shoppingLists.containsKey(list.getId())) {
                list.merge(other.shoppingLists.get(list.getId()));
            }
        }*/
        return this;
    }

    public void removeShoppingList(UUID id) {
        this.shoppingLists.remove(id);
    }

    public void updateShoppingList(UUID shoppingListID, UUID productID, Product product){
        this.shoppingLists.get(shoppingListID).updateProduct(productID, product);
    }

    public void removeProduct(UUID shoppingListID, UUID productID){
        this.shoppingLists.get(shoppingListID).removeProduct(productID);
    }

}
