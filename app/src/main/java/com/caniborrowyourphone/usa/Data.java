package com.caniborrowyourphone.usa;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by Cameron on 12/10/2014.
 */
public class Data extends MainActivity {

    protected static Country currentCountry;
    protected static boolean[][] inUSA;
    protected static byte monthOfLastUpdate, dayOfLastUpdate;
    protected static int numDaysInUSA;
    protected static Calendar today;

}
