package com.example.android.spotifystreamer.util;

import java.util.Locale;

/**
 * Utility class.
 */
public class Utils {

    public static String getDefaultCountry() {
        String country = Locale.getDefault().getCountry();

        // guard non existing country according to
        // https://discussions.udacity.com/t/how-we-should-determine-the-country-code-of-the-user/20947/2
        if (country.equals("")) {
            country = "US";
        }
        return country;
    }
}
