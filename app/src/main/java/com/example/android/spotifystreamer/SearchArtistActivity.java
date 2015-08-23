/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.example.android.spotifystreamer.model.Artist;

public class SearchArtistActivity extends ActionBarActivity
        implements SearchArtistFragment.Callback {

    public static final String TOPTRACKSFRAGMENT_TAG = "TTFAG";
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
}
