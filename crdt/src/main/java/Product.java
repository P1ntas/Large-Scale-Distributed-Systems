import java.util.UUID;

public class Product {
    UUID id;
    String name;
    int quantity;
    VectorClock vectorClock;
    String lastName;

    public Product(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.quantity = 1;
        this.vectorClock = new VectorClock(this.id, System.currentTimeMillis());
        this.lastName = "";
    }

    public Product(String name, int quantity, VectorClock vectorClock) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.quantity = quantity;
        this.vectorClock = vectorClock;
        this.lastName = "";
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
        this.lastName = this.name;
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

    public void mergeProduct(Product other) {

        if (this.name.contains(this.lastName) && (other.name.contains(this.lastName))) {
            this.name = this.name + other.name.replace(this.lastName, "");
        }
        if (this.name.contains(other.lastName) && (other.name.contains(other.lastName))) {
            this.name = this.name + other.name.replace(other.lastName, "");
        }
        // Concatenating the names
        if (!this.name.equals(other.name)) {
            if (other.name.contains(this.name)) {
                this.name = other.name;
            }
            else if (!this.name.contains(other.name)) {
                this.name = this.name + other.name;
            }
        }

        // Determining which product has the larger vector clock
        boolean thisClockIsGreater = this.vectorClock.isGreaterThan(other.vectorClock);

        if (!thisClockIsGreater) {
            // If the other product has the larger clock, adopt its quantity
            this.quantity = other.quantity;
        }

        // Merge the vector clocks
        this.vectorClock.merge(other.vectorClock);
    }
}
