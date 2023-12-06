package com.shopup;

import java.util.Objects;

public class GCounter {
    int counter;
    public GCounter(){
        this.counter = 0;
    }

    public int getCounter() {
        return counter;
    }

    public void add (){
        this.counter++;
    }
    public void merge (GCounter productCounter){
        if (this.counter < productCounter.counter){
            this.counter = productCounter.counter;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GCounter gCounter)) return false;
        return getCounter() == gCounter.getCounter();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCounter());
    }
}
