import java.util.UUID;

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
}
