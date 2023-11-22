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

    public void merge(){

    }
}
