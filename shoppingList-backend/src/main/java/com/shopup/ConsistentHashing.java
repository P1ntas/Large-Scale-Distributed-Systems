package com.shopup;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.SortedMap;
import java.util.TreeMap;
public class ConsistentHashing {
    private final SortedMap<Integer, String> ring;
    private final int n_virtulisation, n_replicas;
    private final MessageDigest md;

    public ConsistentHashing(int numberOfReplicas, int n_virtulisation) throws NoSuchAlgorithmException {
        this.ring = new TreeMap<>();
        this.n_replicas = numberOfReplicas;
        this.n_virtulisation = n_virtulisation;
        this.md = MessageDigest.getInstance("MD5");
    }

    public void addServer(String server) {
        for (int i = 0; i<this.n_virtulisation; i++){
            int hash = getHash(server + i);
            this.ring.put(hash, server);
        }

    }

    public void removeServer(String server) {
        int hash = getHash(server);
        this.ring.remove(hash);
    }

    public String getServer(Object key) {
        if (this.ring.isEmpty()) {
            return null;
        }
        int hash = getHash(key);
        if (!this.ring.containsKey(hash)) {
            SortedMap<Integer, String> tailMap = this.ring.tailMap(hash);
            hash = tailMap.isEmpty() ? this.ring.firstKey() : tailMap.firstKey();
        }
        return this.ring.get(hash);
    }

    private int getHash(Object key) {

        this.md.update(key.toString().getBytes(StandardCharsets.UTF_8));
        byte[] bytes = this.md.digest();

        // Fold the hash into an integer
        int result = ((bytes[3] & 0xFF) << 24)
                | ((bytes[2] & 0xFF) << 16)
                | ((bytes[1] & 0xFF) << 8)
                | (bytes[0] & 0xFF);
        return result;
    }
}
