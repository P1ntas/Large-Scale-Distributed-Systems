package com.shopup;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.SortedMap;
import java.util.TreeMap;
public class ConsistentHashing {
    private final int n_virtulisation;
    private final int n_replication;
    private final MessageDigest md;

    public ConsistentHashing(int n_virtulisation, int n_replication) throws NoSuchAlgorithmException {
        this.n_virtulisation = n_virtulisation;
        this.n_replication = n_replication;
        this.md = MessageDigest.getInstance("MD5");
    }

    public int getN_replication(){
        return this.n_replication;
    }
    public void addServer(String server, TreeMap<Integer, String> ring) {
        int hash = getHash(server);
        ring.put(hash, server);
        for (int i = 1; i < n_virtulisation; i++) {
            //! eventualmente usar um valor random ou uma funcao de has diferente para cada virtual node
            hash = getHash(server + i);
            ring.put(hash, server);
        }
        System.out.println("RING UPDATED + : " + ring);
    }

    public void removeServer(String server, TreeMap<Integer, String> ring) {
        int hash = getHash(server);
        ring.remove(hash);

        System.out.println("RING UPDATED - : " + ring);
    }

    public String getServer(Object key, TreeMap<Integer, String> ring) {
        System.out.println("GETTING SERVER FOR KEY: " + key);
        if (ring.isEmpty()) {
            System.out.println("SERVER RING IS EMPTY, RETURNING NULL");
            return null;
        }
        int hash = getHash(key);
        if (!ring.containsKey(hash)) {
            System.out.println("SERVER NOT IN THE RING, RETURNING NULL");
            return null;
        }
        System.out.println("SERVER FOUND, RETURNING SERVER");
        return ring.get(hash);
    }

    public String getServerAfter(Object key, TreeMap<Integer, String> ring, boolean isServer) {
        int random1 = getHash(key);
        int random2 = getHash(key);
        if(ring.isEmpty()){
            return null;
        }
        int hash = getHash(key);
        SortedMap<Integer, String> tailMap;
        if(isServer){
            tailMap = ring.tailMap(hash + 1);
        }
        else{
            tailMap = ring.tailMap(hash);
        }

        if (!tailMap.isEmpty()) {
            return tailMap.get(tailMap.firstKey());
        }
        else if(isServer && ring.size() == 1 && ring.containsKey(hash)){
            return null;
        }
        return ring.get(ring.firstKey());
    }

    int getHash(Object key) {

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
