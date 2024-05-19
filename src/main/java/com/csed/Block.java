package com.csed;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Wraps the bytes array to offer hashing
 * so that it can be used as a key for hashmap efficiently
 * <a href="https://www.baeldung.com/java-map-key-byte-array">reference</a>
 * @param bytes
 */
public record Block(byte[] bytes) implements Serializable {
    public Block(byte[] bytes) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        return Arrays.equals(this.bytes, ((Block) o).bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    // for debugging
    @Override
    public String toString() {
        return Arrays.toString(bytes);
    }
}
