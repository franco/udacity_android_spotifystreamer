/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Locale;

/**
 * Utility class.
 */
public class Utils {

    private static boolean sNetworkAvailable;

    public static String getDefaultCountry() {
        String country = Locale.getDefault().getCountry();

        // guard non existing country according to
        // https://discussions.udacity.com/t/how-we-should-determine-the-country-code-of-the-user/20947/2
        if (country.equals("")) {
            country = "US";
        }
        return country;
    }

    /** Returns if network is available */
    public static boolean isNetworkAvailable(Context context) {
        // Copied from
        // https://udacity-github-sync-content.s3.amazonaws.com/_imgs/752/1436612063/Screen_Shot_2015-07-11_at_12.53.52.png
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
