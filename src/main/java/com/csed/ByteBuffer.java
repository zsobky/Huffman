package com.csed;

public class ByteBuffer {
    byte b;
    short available;

    public ByteBuffer() {
        this.b = 0;
        this.available = 8;
    }

    public void append(boolean bit) {
        if (available == 0) throw new RuntimeException("Byte buffer overflow");

        available--;
        if (bit)
            b |= 1 << available;
    }

    public byte extract() {
        byte temp = b;
        b = 0;
        available = 8;
        return temp;
    }

    public boolean isFullByte() {
        return available == 0;
    }

    public boolean isEmpty() {
        return available == 8;
    }

    @Override
    public String toString() {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF))
                .replace(' ', '0');
    }
}
