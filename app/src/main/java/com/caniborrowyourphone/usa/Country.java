package com.caniborrowyourphone.usa;

/**
 * Enum for which country, either Canada or USA.
 * Created by Cameron Johnston on 12/5/2014
 */
public enum Country {
	CANADA ((byte)0x00), USA ((byte)0x01);

    private final byte value;

    protected byte getValue() { return value; }

    Country(byte b) { this.value = b; }
}
