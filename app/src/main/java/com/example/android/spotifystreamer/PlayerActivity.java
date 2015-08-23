/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

public class PlayerActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Bundle arguments = new Bundle();
        arguments.putParcelable(PlayerFragment.EXTRA_ARTIST,
                getIntent().getParcelableExtra(PlayerFragment.EXTRA_ARTIST));
        arguments.putParcelableArrayList(PlayerFragment.EXTRA_TRACKS,
                getIntent().getParcelableArrayListExtra(PlayerFragment.EXTRA_TRACKS));
        arguments.putInt(PlayerFragment.EXTRA_CURRENT_TRACK_POSITION,
                getIntent().getIntExtra(PlayerFragment.EXTRA_CURRENT_TRACK_POSITION, 0));

        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(arguments);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_muisc_player, fragment)
                .commit();
    }
}
