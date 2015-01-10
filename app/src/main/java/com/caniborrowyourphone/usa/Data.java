package com.caniborrowyourphone.usa;

import java.util.Calendar;

/**
 * Class for sharing static data between all activities of USA Days Monitor app
 * Created by Cameron Johnston on 12/10/2014.
 */
public class Data {
    private static final String tag = "Data";

    static final boolean USING_DUMMY_LOCATION_SERVICES = false;

    static final int NUM_BYTES_FOR_STORING_DAYS = 12*31;
    static final String FILENAME_DAYS = "usa_days_records.txt";
    static final String FILENAME_COUNTRY = "current_country.txt";
    static final String FILENAME_TIMESTAMP = "timestamp.txt";
    static final String FILENAME_EMAIL = "email.txt";
    static final String FILENAME_MODE = "mode.txt";

    static Country currentCountry;
    static boolean[][] inUSA;
    static byte monthOfLastUpdate, dayOfLastUpdate;
    static int numDaysInUSA;
    static Calendar today;

    static Mode mode;
    static String email;
    static boolean loggedIn, justLoggedOut;
}
