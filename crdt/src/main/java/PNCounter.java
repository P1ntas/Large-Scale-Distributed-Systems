public class PNCounter {
    GCounter positiveGCounter;
    GCounter negativeGCounter;

    public PNCounter() {
        this.positiveGCounter = new GCounter();
        this.negativeGCounter = new GCounter();
    }

    public void increment(){
        this.positiveGCounter.add();
    }

    public void decrement(){
        this.negativeGCounter.add();
    }

    public void merge(PNCounter other){
        if (this.positiveGCounter.counter < other.positiveGCounter.counter){
            this.positiveGCounter.counter = other.positiveGCounter.counter;
        }
        if (this.negativeGCounter.counter < other.negativeGCounter.counter){
            this.negativeGCounter.counter = other.negativeGCounter.counter;
        }
    }

    public int value(){
        return this.positiveGCounter.counter - this.negativeGCounter.counter;
    }
}
