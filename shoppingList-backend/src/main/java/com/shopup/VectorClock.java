package com.shopup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;

class VectorClock {
    private UUID id;
    private long timestamp;

    @JsonCreator
    public VectorClock(@JsonProperty("id") UUID id,
                       @JsonProperty("timestamp") long timestamp) {
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

    public VectorClock merge(VectorClock other) {
        if (this.timestamp < other.timestamp) {
            this.timestamp = other.timestamp;
        }

        return this;
    }

    public boolean isEqual(VectorClock other) {
        return this.timestamp == other.timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VectorClock that)) return false;
        return getTimestamp() == that.getTimestamp() && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTimestamp());
    }
}
