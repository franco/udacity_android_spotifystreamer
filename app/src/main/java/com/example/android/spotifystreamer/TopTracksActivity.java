/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;

import com.example.android.spotifystreamer.model.Artist;
import com.example.android.spotifystreamer.model.MyTrack;

import java.util.ArrayList;

/**
 * Activity that holds the TopTracks fragment. It extends from BaseActivity which handles
 * now-playing button and connection to PlayerService.
 */
public class TopTracksActivity extends BaseActivity
        implements TopTracksFragment.OnTrackSelectedListener {

    public static final String EXTRA_ARTIST = "artist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        if (savedInstanceState == null) {
            Artist artist = getIntent().getParcelableExtra(EXTRA_ARTIST);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_top_tracks, TopTracksFragment.newInstance(artist))
                    .commit();
        }
    }

    @Override
    public void onTrackSelected(Artist artist, ArrayList<MyTrack> tracks, int position) {
        if (isPlayerBound()) {
            getPlayerService().playTracks(tracks, position, artist);
            getMediaControllerCompat().getTransportControls().play();
            startActivity(new Intent(this, FullScreenPlayerActivity.class));
        }
    }
}
