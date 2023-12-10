package com.shopup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

import static com.shopup.Utils.mergeNames;

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

    public ShoppingList(String name, UUID id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShoppingList that)) return false;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName()) && Objects.equals(getProducts(), that.getProducts());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getProducts());
    }

    public ShoppingList merge(ShoppingList other) {
        if (this.equals(other)) return this;

        ShoppingList newShoppingList = new ShoppingList(this.getName(), this.getId());

        /* for each product in products use the merge function of the Product class to merge products of this and other with the same id */
        for (Product product : this.products.values()) {

            System.out.println("CHECKING THIS PRODUCT: " + product.getName());
            if (other.products.containsKey(product.getId())) {
                if (product.merge(other.products.get(product.getId())) != null){
                    newShoppingList.addProduct(product);
                }
            }else{
                Product newProduct = new Product(product.getName(),product.getId(),product.getPnCounter());
                other.addProduct(newProduct);
                newShoppingList.addProduct(product);
                product.merge(other.products.get(product.getId()));
            }
        }
        for(Product product : other.getProducts().values()){

            System.out.println("CHECKING THIS PRODUCT: " + product.getName());

            if (this.products.containsKey(product.getId())) {
                if (product.merge(other.products.get(product.getId())) != null){
                    newShoppingList.addProduct(product);
                }
            }else{
                Product newProduct = new Product(product.getName(), product.getId(), product.getPnCounter());
                addProduct(newProduct);
                newShoppingList.addProduct(product);
                product.merge(other.products.get(product.getId()));
            }


/*            System.out.println("Printing the new shopping list\n");
            for(Product x : newShoppingList.getProductList()){
                System.out.println(x.getName() + ": " + x.getQuantity());
            }
            System.out.println("Finished");*/
        }

/*        System.out.println("\nTHIS PRINT IS TESTING INSIDE THE SHOPPING LIST MERGE\n");
        for(Product product : newShoppingList.getProductList()){
            System.out.println(product.getName() + ": " + product.getQuantity());
        }*/

        return newShoppingList;
    }
}