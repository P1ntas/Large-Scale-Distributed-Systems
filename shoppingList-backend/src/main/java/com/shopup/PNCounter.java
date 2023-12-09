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

    @JsonCreator
    public PNCounter(@JsonProperty("positiveGCounter") Map<UUID,Integer> positiveGCounter,
                     @JsonProperty("negativeGCounter") Map<UUID,Integer> negativeGCounter){
        this.positiveGCounter = new GCounter();
        this.negativeGCounter = new GCounter();
        this.positiveGCounter.setCounter(positiveGCounter);
        this.negativeGCounter.setCounter(negativeGCounter);
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

    public int getValue(){
        int value = positiveGCounter.getValue() - negativeGCounter.getValue();
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
