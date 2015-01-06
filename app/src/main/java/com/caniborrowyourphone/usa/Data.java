package com.caniborrowyourphone.usa;

import java.util.Calendar;

/**
 * Class for sharing static data between all activities of USA Days Monitor app
 * Created by Cameron Johnston on 12/10/2014.
 */
public class Data extends MainActivity {

    protected static final boolean USING_DUMMY_LOCATION_SERVICES = false;

    protected static final int NUM_BYTES_FOR_STORING_DAYS = 12*31;
    protected static final String FILENAME_DAYS = "usa_days_records.txt";
    protected static final String FILENAME_COUNTRY = "current_country.txt";
    protected static final String FILENAME_TIMESTAMP = "timestamp.txt";

    protected static Country currentCountry;
    protected static boolean[][] inUSA;
    protected static byte monthOfLastUpdate, dayOfLastUpdate;
    protected static int numDaysInUSA;
    protected static Calendar today;

    protected static boolean usingCloudStorage;
    protected static boolean creatingAccount;
    protected static String username;
}
