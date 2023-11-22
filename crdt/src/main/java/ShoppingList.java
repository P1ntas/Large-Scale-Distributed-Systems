import java.util.HashMap;
import java.util.UUID;

class ShoppingList {
    UUID id;
    String name;
    HashMap<UUID, Product> products;

    // Constructor, getters and setters
    public ShoppingList(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.products = new HashMap<>();
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public HashMap<UUID, Product> getProducts() {
        return this.products;
    }

    //setters
    public void setName(String name) {
        this.name = name;
    }

    public void setProducts(HashMap<UUID, Product> products) {
        this.products = products;
    }

    // Methods
    public void addProduct(Product product) {
        this.products.put(product.getId(), product);
    }

    public void removeProduct(UUID id) {
        this.products.remove(id);
    }

    public void updateProduct(UUID id, Product product) {
        this.products.put(id, product);
    }
}