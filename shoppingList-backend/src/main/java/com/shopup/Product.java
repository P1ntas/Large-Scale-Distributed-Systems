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

    public Product(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.quantity = 1;
        this.vectorClock = new VectorClock(this.id, System.currentTimeMillis());
    }

    public Product(String name, UUID id, int quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.vectorClock = new VectorClock(this.id, System.currentTimeMillis());
    }

    public Product(String name, UUID id, int quantity, long timestamp) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.vectorClock = new VectorClock(this.id, timestamp);
    }

    public Product(String name, int quantity, VectorClock vectorClock) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.quantity = quantity;
        this.vectorClock = vectorClock;
    }

    @JsonCreator
    public Product(@JsonProperty("name") String name,
                   @JsonProperty("id") UUID id,
                   @JsonProperty("quantity") int quantity,
                   @JsonProperty("vectorClock") VectorClock vectorClock) {
        this.name = name;
        this.id = id != null ? id : UUID.randomUUID();
        this.quantity = quantity;
        this.vectorClock = vectorClock;
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public VectorClock getVectorClock() {
        return this.vectorClock;
    }

    //setters
    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity = quantity;
    }

    public void setVectorClock(VectorClock vectorClock) {
        this.vectorClock = vectorClock;
    }

    // Methods
    public void incrementQuantity() {
        this.quantity++;
    }

    public void decrementQuantity() {
        if (this.quantity == 1) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity--;
    }

    public Product merge(Product other) {
        if (this.equals(other)) return this;

        this.name = mergeNames(this.name, other.name);

        if (this.vectorClock.getTimestamp() > other.vectorClock.getTimestamp()) {

            this.quantity = other.quantity;
        }
        else if (this.vectorClock.getTimestamp() == other.vectorClock.getTimestamp()) {
            this.quantity = Math.max(this.quantity, other.quantity);
        }

        this.vectorClock.merge(other.vectorClock);

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return getQuantity() == product.getQuantity() && Objects.equals(getId(), product.getId()) && Objects.equals(getName(), product.getName()) && Objects.equals(getVectorClock(), product.getVectorClock());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getQuantity(), getVectorClock());
    }
}
