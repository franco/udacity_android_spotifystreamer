/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Full screen player that shows the current playing music with its artwork and controls
 * for seek/pause/play.
 */
public class FullScreenPlayerActivity extends AppCompatActivity {

    private static final String LOG_TAG = FullScreenPlayerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_muisc_player, new PlayerFragment())
                    .commit();
        }
    }
}
