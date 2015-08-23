/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.example.android.spotifystreamer.model.Artist;
import com.example.android.spotifystreamer.model.MyTrack;

import java.util.ArrayList;


public class TopTracksActivity extends ActionBarActivity
        implements TopTracksFragment.TrackSelectedCallback {

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

    @Override
    public void onTrackSelected(Artist artist, ArrayList<MyTrack> tracks, int position) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerFragment.EXTRA_TRACKS, tracks);
        intent.putExtra(PlayerFragment.EXTRA_ARTIST, artist);
        intent.putExtra(PlayerFragment.EXTRA_CURRENT_TRACK_POSITION, position);
        startActivity(intent);
    }
}
