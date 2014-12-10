package com.caniborrowyourphone.usa;

public enum Country {
	CANADA ((byte)0x00), USA ((byte)0x01);

    private final byte value;

    protected byte getValue() { return value; }

    Country(byte b) { this.value = b; }
}
