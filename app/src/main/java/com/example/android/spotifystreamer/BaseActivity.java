/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.spotifystreamer.service.PlayerService;

/**
 * Base activity that manages the connection to the player service and listen to
 * playbackStateChanges in order to show/hide the now playing button.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String LOG_TAG = SearchArtistActivity.class.getSimpleName();

    private Intent mPlayIntent;
    private PlayerService mPlayerService;
    private MediaControllerCompat mMediaController;
    private boolean mPlayerBound;
    private boolean mShowPlayButton;

    /**
     * MediaController Callback to listen for playbackStateChanges in order to show/hide now
     * playing menu item.
     */
    private final MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback(){
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            mShowPlayButton = state.getState() == PlaybackStateCompat.STATE_PLAYING;
            invalidateOptionsMenu();
        }
    };

    /**
     * Service callback. When bound to services it creates also a MediaController.
     */
    private ServiceConnection playerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(LOG_TAG, "Service Connection established");
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) iBinder;
            mPlayerService = binder.getService();
            mPlayerBound = true;
            connectToMediaSession(mPlayerService.getSessionToken());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // This method gets only called when the Server has crashed or been killed.
            mPlayerBound = false;
            Log.d(LOG_TAG, "Service Connection disconncted");
        }
    };

    private void connectToMediaSession(MediaSessionCompat.Token token) {
        Log.d(LOG_TAG, "connectToMediaSession");
        try {
            mMediaController = new MediaControllerCompat(this, token);
            mMediaController.registerCallback(mCallback);

            // Update now-playing button
            PlaybackStateCompat state = mMediaController.getPlaybackState();
            mShowPlayButton = state != null && mMediaController.getPlaybackState().getState()
                    == PlaybackStateCompat.STATE_PLAYING;
            invalidateOptionsMenu();

        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Cannot access session");
        }
    }

    protected PlayerService getPlayerService() {
        return mPlayerService;
    }

    protected MediaControllerCompat getMediaControllerCompat() {
        return mMediaController;
    }

    protected boolean isPlayerBound() {
        return mPlayerBound;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayIntent = new Intent(this, PlayerService.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(mPlayIntent, playerConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPlayerBound) {
            mMediaController.unregisterCallback(mCallback);
            unbindService(playerConnection);
            mPlayerBound = false;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_now_playing);
        item.setVisible(mShowPlayButton);
        return super.onPrepareOptionsMenu(menu);
    }
}
