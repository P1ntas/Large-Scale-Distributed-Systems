package com.shopup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

class ShoppingList {
    UUID id;
    String name;
    HashMap<UUID, Product> products;

    // Constructor, getters and setters
    public ShoppingList(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.products = new HashMap<>();
    }

    public ShoppingList(String name, UUID id, HashMap<UUID, Product> products) {
        this.id = id;
        this.name = name;
        this.products = products;
    }

    @JsonCreator
    public ShoppingList(@JsonProperty("name") String name,
                        @JsonProperty("id") UUID id,
                        @JsonProperty("products") List<Product> productList) {
        this.name = name;
        this.id = id;
        this.products = new HashMap<>();
        if (productList != null) {
            for (Product product : productList) {
                this.products.put(product.getId(), product);
            }
        }
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public HashMap<UUID, Product> getProducts() {
        return this.products;
    }

    //setters
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("products")
    public List<Product> getProductList() {
        return new ArrayList<>(this.products.values());
    }

    public void setProducts(HashMap<UUID, Product> products) {
        this.products = products;
    }

    // Methods
    public void addProduct(Product product) {
        this.products.put(product.getId(), product);
    }

    public void removeProduct(UUID id) {
        this.products.remove(id);
    }

    public void updateProduct(UUID id, Product product) {
        this.products.put(id, product);
    }
}