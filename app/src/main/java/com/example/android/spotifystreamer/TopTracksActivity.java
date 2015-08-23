/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class TopTracksActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            arguments.putParcelable(TopTracksFragment.EXTRA_ARTIST,
                    getIntent().getParcelableExtra(TopTracksFragment.EXTRA_ARTIST));

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_top_tracks, fragment)
                    .commit();
        }
    }
}
