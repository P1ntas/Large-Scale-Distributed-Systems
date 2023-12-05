package org.crdt;

import java.util.UUID;

class VectorClock {
    private UUID id;
    private long timestamp;

    public VectorClock(UUID id, long timestamp) {

        this.id = id;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return this.id;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    // setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void updateClock(UUID nodeId, int timestamp) {
        this.timestamp = timestamp;
    }

    public void merge(VectorClock other) {
        if (this.timestamp > other.timestamp) {
            this.timestamp = other.timestamp;
        }
        else if (this.timestamp < other.timestamp) {
            this.timestamp = other.timestamp;
        }
    }

    // Helper method to compare two vector clocks
    public boolean isLessThan(VectorClock other) {
        return this.timestamp < other.timestamp;
    }

    public boolean isGreaterThan(VectorClock other) {
        return this.timestamp > other.timestamp;
    }

    public boolean isEqual(VectorClock other) {
        return this.timestamp == other.timestamp;
    }
}
