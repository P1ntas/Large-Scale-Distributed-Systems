package com.shopup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.lang.Math.max;

public class GCounter {
    Map<UUID, Integer> counter;
    public GCounter(){
        this.counter = new HashMap<UUID,Integer>();
    }

    public GCounter(UUID userId){
        this.counter = new HashMap<UUID,Integer>();
        this.counter.put(userId,0);
    }
    @JsonCreator
    public GCounter(@JsonProperty("counter") Map<UUID,Integer> counter){
        this.counter = counter;
    }

    public Map<UUID, Integer> getCounter() {
        return counter;
    }

    public void setCounter(Map<UUID, Integer> counter) {
        this.counter = counter;
    }

    public void add (UUID key, Integer value){
        Integer count = counter.get(key);
        if( count == null )
            count = 0;

        counter.put(key, count + value);
    }

    public int calculateValue(){
        int sum = 0;
        for(int i: counter.values()){
            sum+=i;
        }
        return sum;
    }

    public void merge(GCounter gCounter2){
        for(Map.Entry<UUID,Integer> x: gCounter2.counter.entrySet()){
            UUID id = x.getKey();
            //Select the maximum value from each key in the hashmap, if it doesn't exists just insert the upcoming value
            if(counter.containsKey(id)){
                counter.put(id,max(x.getValue(),counter.get(id)));
                gCounter2.getCounter().put(id,max(x.getValue(),counter.get(id)));
            }
            else{
                counter.put(id,x.getValue());
                gCounter2.getCounter().put(id,x.getValue());
            }
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
