/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.spotifystreamer.model.Artist;
import com.example.android.spotifystreamer.model.MyTrack;
import com.example.android.spotifystreamer.service.PlayerService;

import java.util.ArrayList;

public class SearchArtistActivity extends ActionBarActivity
        implements SearchArtistFragment.Callback, TopTracksFragment.TrackSelectedCallback {

    public static final String LOG_TAG = SearchArtistActivity.class.getSimpleName();
    public static final String TOPTRACKSFRAGMENT_TAG = "TTFAG";
    public static final String PLAYERFRAGMENT_TAG = "PFAG";

    private boolean mTwoPane;
    private PlayerService mPlayerService;
    private boolean mIsPlayerBound;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_now_playing);
        boolean shouldDisplayMenu = mPlayerService != null && mPlayerService.isPlaying();
        Log.d(LOG_TAG, "shouldDisplayMenu=" + shouldDisplayMenu);
        item.setVisible(mPlayerService != null && mPlayerService.isPlaying());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_now_playing) {
            if (mTwoPane) {
                new PlayerFragment().show(getSupportFragmentManager(), PLAYERFRAGMENT_TAG);
            } else {
                startActivity(new Intent(this, PlayerActivity.class));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mIsPlayerBound) {
            Intent playIntent = new Intent(this, PlayerService.class);
            bindService(playIntent, playerConnection, 0); // only bind if service already exists
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayerService.MUSIC_PLAYER_PLAYBACK_STARTED_NOTIFICATION);
        filter.addAction(PlayerService.MUSIC_PLAYER_PLAYBACK_STOPPED_NOTIFICATION);

        LocalBroadcastManager.getInstance(this).registerReceiver(mOnMusicPlayerNotification, filter);

        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mOnMusicPlayerNotification);

        if (mIsPlayerBound) {
            unbindService(playerConnection);
            mIsPlayerBound = false;
        }
        super.onPause();
    }

    private ServiceConnection playerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) iBinder;
            mPlayerService = binder.getService();
            mIsPlayerBound = true;

            if (mPlayerService.isPlaying()) {
                invalidateOptionsMenu();
            }

            Log.d(LOG_TAG, "Service Connection established");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // This method gets only called when the Server has crashed or been killed.
            mIsPlayerBound = false;
            Log.d(LOG_TAG, "Service Connection disconncted");
        }
    };

    private BroadcastReceiver mOnMusicPlayerNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()) {
                case PlayerService.MUSIC_PLAYER_PLAYBACK_STARTED_NOTIFICATION:
                    Log.d(LOG_TAG, "player preparing");
                    invalidateOptionsMenu();
                    break;
                case PlayerService.MUSIC_PLAYER_PLAYBACK_STOPPED_NOTIFICATION:
                    Log.d(LOG_TAG, "player stopped");
                    invalidateOptionsMenu();
                    break;
            }
        }
    };
}
