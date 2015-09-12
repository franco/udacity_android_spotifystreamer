/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.android.spotifystreamer.model.Artist;
import com.example.android.spotifystreamer.model.MyTrack;

import java.util.ArrayList;

/**
 * Main activity that holds the SearchArtistFragment on small screen and on tablet size screen
 * it displays SearchArtistFragment and TopTracksFragment side by side in a master-detail view.
 * In two-pain mode the activity controls the player on request from the TopTracks list. It extends
 * from BaseActivity which handles now-playing button and connection to PlayerService.
 */
public class SearchArtistActivity extends BaseActivity
        implements SearchArtistFragment.ArtistSelectedCallback, TopTracksFragment.TrackSelectedCallback {

    public static final String TOPTRACKSFRAGMENT_TAG = "TTFAG";
    public static final String PLAYERFRAGMENT_TAG = "PFAG";

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_artist);

        // Add TopTracks in two-pane mode
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
    public void onArtistSelected(Artist artist) {
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
        if (isPlayerBound()) {
            getPlayerService().playTracks(tracks, position, artist);
            getMediaControllerCompat().getTransportControls().play();
            new PlayerFragment().show(getSupportFragmentManager(), PLAYERFRAGMENT_TAG);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_now_playing) {
            if (mTwoPane) {
                new PlayerFragment().show(getSupportFragmentManager(), PLAYERFRAGMENT_TAG);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
