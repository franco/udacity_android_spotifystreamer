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


public class TopTracksActivity extends ActionBarActivity
        implements TopTracksFragment.TrackSelectedCallback {

    private static final String LOG_TAG = TopTracksActivity.class.getSimpleName();

    private PlayerService mPlayerService;
    private boolean mIsPlayerBound;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_now_playing);
        item.setVisible(mPlayerService != null && mPlayerService.isPlaying());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_now_playing) {
            startActivity(new Intent(this, PlayerActivity.class));
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

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mOnMusicPlayerStopped,
                new IntentFilter(PlayerService.MUSIC_PLAYER_PLAYBACK_STOPPED_NOTIFICATION));

        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mOnMusicPlayerStopped);

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

    private BroadcastReceiver mOnMusicPlayerStopped = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TopTrackActivity", "player stopped");
            invalidateOptionsMenu();
        }
    };
}
