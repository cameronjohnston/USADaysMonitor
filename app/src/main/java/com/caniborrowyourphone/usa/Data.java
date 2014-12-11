package com.caniborrowyourphone.usa;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Cameron on 12/10/2014.
 */
public class Data extends MainActivity {

    protected Country currentCountry;
    protected boolean[][] inUSA;
    protected byte monthOfLastUpdate, dayOfLastUpdate;
    protected int numDaysInUSA;

    public Data() {

        inUSA = new boolean[12][31];
        numDaysInUSA = 0;

        monthOfLastUpdate = 0x0; // Should always be between 0-11 inclusive
        dayOfLastUpdate = 0x0; // Should always be between 0-30 inclusive
    }
}
