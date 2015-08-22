/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.android.spotifystreamer.R;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

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

    /** Returns if network is available */
    public static boolean isNetworkAvailable(Context context) {
        // Copied from
        // https://udacity-github-sync-content.s3.amazonaws.com/_imgs/752/1436612063/Screen_Shot_2015-07-11_at_12.53.52.png
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /** Displays an alert dialog */
    public static void showAlertDialog(Context context, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(R.string.error_title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,
                context.getResources().getString(R.string.neutral_button_label),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public static void showAlertDialog(Context context, int messageId) {
        showAlertDialog(context, context.getResources().getString(messageId));
    }

    /**
     * Returns a formatted time string (m:ss).
     *
     * Copied from http://stackoverflow.com/a/625624
     */
    public static String formatScrubBarTime(int duration_in_ms) {
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration_in_ms),
                TimeUnit.MILLISECONDS.toSeconds(duration_in_ms) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration_in_ms))
        );
    }
}
