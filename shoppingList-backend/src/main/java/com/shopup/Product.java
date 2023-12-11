package com.shopup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;

import static com.shopup.Utils.mergeNames;

public class Product {
    UUID id;
    String name;
    int quantity;
    VectorClock vectorClock;
    PNCounter pnCounter;

    public Product(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.quantity = 1;
        this.vectorClock = new VectorClock(this.id, System.currentTimeMillis());
        this.pnCounter = new PNCounter();
    }

    /*
    * This constructor is used when a user adds a new product to a shopping list
    * */
    public Product(String name, UUID userId, int quantity) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.pnCounter = new PNCounter(userId);
        setQuantity(userId,quantity);
        this.quantity = getQuantity();
        this.vectorClock = new VectorClock(this.id, System.currentTimeMillis());
    }

    public Product(String name, PNCounter newPNCounter) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.pnCounter = newPNCounter;
        this.quantity = getQuantity();
        this.vectorClock = new VectorClock(this.id, System.currentTimeMillis());
    }
    /*
     * This constructor is used when a merge between two shopping lists is done and a new product is needed
     * */
    public Product(String name, UUID id, PNCounter newPNCounter) {
        this.id = id;
        this.name = name;
        this.pnCounter = newPNCounter;
        this.quantity = getQuantity();
        this.vectorClock = new VectorClock(this.id, System.currentTimeMillis());
    }

    @JsonCreator
    public Product(@JsonProperty("name") String name,
                   @JsonProperty("id") UUID id,
                   @JsonProperty("quantity") int quantity,
                   @JsonProperty("vectorClock") VectorClock vectorClock,
                   @JsonProperty("pnCounter") PNCounter pnCounter) {
        this.name = name;
        this.id = id != null ? id : UUID.randomUUID();
        this.quantity = quantity;
        this.vectorClock = vectorClock;
        this.pnCounter = pnCounter;


    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getQuantity() {
        return this.pnCounter.calculateValue();
    }

    public VectorClock getVectorClock() {
        return this.vectorClock;
    }

    public PNCounter getPnCounter() {
        return pnCounter;
    }

    //setters
    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(UUID userID,int quantity) {
        if (quantity < 1) {
            System.out.println("Cannot add or decrement number smaller than 1");
            return;
        } else if (quantity == getQuantity()) {
            System.out.println("Your input is the same has the product quatity");
        }

        System.out.println("ESTA É A NOVA QUANTIDADE: "+ quantity);

        if(quantity < getQuantity()){
            decrementQuantity(userID, this.quantity - quantity);
        }else{
            incrementQuantity(userID,quantity - this.quantity);
        }

/*        if(this.quantity - quantity > 0){
            decrementQuantity(userID, this.quantity - quantity);
        }else{
            incrementQuantity(userID,quantity - this.quantity);
        }*/
    }

    public void setVectorClock(VectorClock vectorClock) {
        this.vectorClock = vectorClock;
    }

    // Methods

    public void incrementQuantity(UUID userID, int added) {
        this.pnCounter.increment(userID, added);
        this.quantity = this.pnCounter.calculateValue();
    }

    public void decrementQuantity(UUID userID, int added) {
        this.pnCounter.decrement(userID, added);
        this.quantity = this.pnCounter.calculateValue();
    }

    public void removeProduct(UUID userId) {
        decrementQuantity(userId,this.getQuantity());
    }

    public Product merge(Product other) {


        this.vectorClock.setTimestamp(System.currentTimeMillis());
        other.getVectorClock().setTimestamp(System.currentTimeMillis());

        pnCounter.merge(other.getPnCounter());

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return getQuantity() == product.getQuantity() &&
                Objects.equals(getId(), product.getId()) &&
                Objects.equals(getName(), product.getName()) &&
                Objects.equals(getVectorClock(), product.getVectorClock()) &&
                Objects.equals(getPnCounter(), product.getPnCounter());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getQuantity(), getVectorClock());
    }
}
