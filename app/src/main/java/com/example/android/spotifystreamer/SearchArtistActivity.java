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

public class SearchArtistActivity extends ActionBarActivity
        implements SearchArtistFragment.Callback, TopTracksFragment.TrackSelectedCallback {

    public static final String TOPTRACKSFRAGMENT_TAG = "TTFAG";
    public static final String PLAYERFRAGMENT_TAG = "PFAG";

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_artist);

        if (findViewById(R.id.fragment_top_tracks) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_top_tracks, new TopTracksFragment(), TOPTRACKSFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

    }

    @Override
    public void onItemSelected(Artist artist) {
        if (mTwoPane) {
            // In two-pain mode, show the top-tracks view in this activity by adding or replacing
            // the top-track fragment using a fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(TopTracksFragment.EXTRA_ARTIST, artist);

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_top_tracks, fragment, TOPTRACKSFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, TopTracksActivity.class)
                    .putExtra(TopTracksFragment.EXTRA_ARTIST, artist);
            startActivity(intent);
        }
    }

    @Override
    public void onTrackSelected(Artist artist, ArrayList<MyTrack> tracks, int position) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(PlayerFragment.EXTRA_TRACKS, tracks);
        args.putParcelable(PlayerFragment.EXTRA_ARTIST, artist);
        args.putInt(PlayerFragment.EXTRA_CURRENT_TRACK_POSITION, position);

        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
        fragment.show(getSupportFragmentManager(), PLAYERFRAGMENT_TAG);
    }
}
