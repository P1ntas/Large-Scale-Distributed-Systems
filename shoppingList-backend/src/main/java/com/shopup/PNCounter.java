package com.shopup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PNCounter {
    GCounter positiveGCounter;
    GCounter negativeGCounter;

    public PNCounter() {
        this.positiveGCounter = new GCounter();
        this.negativeGCounter = new GCounter();
    }

    public PNCounter(UUID userId) {
        this.positiveGCounter = new GCounter(userId);
        this.negativeGCounter = new GCounter(userId);
    }

    @JsonCreator
    public PNCounter(@JsonProperty("positiveGCounter") GCounter positiveGCounter,
                     @JsonProperty("negativeGCounter") GCounter negativeGCounter){
        this.positiveGCounter = new GCounter(positiveGCounter.getCounter());
        this.negativeGCounter = new GCounter(negativeGCounter.getCounter());
    }

    public GCounter getPositiveGCounter(){
        return this.positiveGCounter;
    }

    public GCounter getNegativeGCounter(){
        return this.negativeGCounter;
    }

    public GCounter setPositiveGCounter(GCounter positiveGCounter){
        return this.positiveGCounter = positiveGCounter;
    }

    public GCounter setNegativeGCounter(GCounter negativeGCounter){
        return this.negativeGCounter = negativeGCounter;
    }

    public void increment(UUID id, Integer value){
        positiveGCounter.add(id, value);
    }

    public void decrement(UUID id, Integer value){
        negativeGCounter.add(id, value);
    }

    public void merge(PNCounter other){
        positiveGCounter.merge(other.positiveGCounter);
        negativeGCounter.merge(other.negativeGCounter);
    }

    public int calculateValue(){
        int value = positiveGCounter.calculateValue() - negativeGCounter.calculateValue();
        return Math.max(value, 0);
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PNCounter pnCounter)) return false;
        return Objects.equals(getPositiveGCounter(), pnCounter.getPositiveGCounter()) && Objects.equals(getNegativeGCounter(), pnCounter.getNegativeGCounter());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPositiveGCounter(), getNegativeGCounter());
    }
}
