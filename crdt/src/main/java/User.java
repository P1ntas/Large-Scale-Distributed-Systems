import java.util.UUID;

public class User {
    String username;
    UUID id;
    PNCounter counter;

    public User(String username) {
        this.username = username;
        this.id = UUID.randomUUID();
        this.counter = new PNCounter();
    }

    public String getUsername() {
        return this.username;
    }

    public UUID getId() {
        return this.id;
    }

    public PNCounter getCounter() {
        return this.counter;
    }

    //setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPNCounter(PNCounter counter) {
        this.counter = counter;
    }
}
