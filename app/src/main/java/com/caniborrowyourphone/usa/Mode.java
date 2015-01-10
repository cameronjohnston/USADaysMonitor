package com.caniborrowyourphone.usa;

/**
 * Created by Cameron on 1/8/2015.
 */
public enum Mode {
    REGULAR ((byte)0x00), MINIMIZE_DATA_USAGE ((byte)0x01);

    private final byte value;

    byte getValue() { return value; }

    Mode(byte b) { this.value = b; }
}
