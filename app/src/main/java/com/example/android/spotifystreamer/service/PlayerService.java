/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.example.android.spotifystreamer.MediaNotificationManager;
import com.example.android.spotifystreamer.Playback;
import com.example.android.spotifystreamer.R;
import com.example.android.spotifystreamer.model.Artist;
import com.example.android.spotifystreamer.model.MyTrack;

import java.util.ArrayList;
import java.util.List;


/**
 * This class manages the playback of songs. It creates a MediaSession and exposes it through its
 * MediaSession.Token, which allows th eclient to create a MediaController that connects to and
 * send control commands to the Media Session remotely.
 */
public class PlayerService extends Service implements Playback.Callback {

    public static final String MUSIC_PLAYER_SONG_CHANGED_NOTIFICATION =
            "com.example.android.spotifystreamer.MUSIC_PLAYER_NOTIFICATION.SONG_CHANGED";
    public static final String SONG_POSITION_EXTRA = "song_position_exra";
    public static final String ARTIST_EXTRA = "artist_exra";
    private static final String MEDIA_SESSION_TAG = "media_session";
    private final IBinder mPlayerBinder = new PlayerBinder();
    private int mTrackPosition;
    private ArrayList<MyTrack> mTracks;
    private Artist mArtist;
    private MediaSessionCompat mSession;
    private List<MediaSessionCompat.QueueItem> mPlayingQueue;
    private Playback mPlayback;
    private MediaNotificationManager mMediaNotificationManager;
    private boolean mShowNotification;

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayingQueue = new ArrayList<>();
        mTrackPosition = 0;
        mPlayback = new Playback(this);
        mPlayback.setCallback(this);

        // Start a new MediaSession
        mSession = new MediaSessionCompat(getApplicationContext(), MEDIA_SESSION_TAG, null, null);
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mMediaNotificationManager = new MediaNotificationManager(this);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mShowNotification = mPrefs.getBoolean(getString(R.string.pref_show_notification_key),
                getResources().getBoolean(R.bool.pref_show_notification_default));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSession.release();
    }

    public MediaSessionCompat.Token getSessionToken() {
        return mSession.getSessionToken();
    }

    public void playTracks(ArrayList<MyTrack> tracks, int trackPosition, Artist artist) {

        mTracks = tracks;
        mTrackPosition = trackPosition;
        mArtist = artist;
        mPlayingQueue = new ArrayList<>();
        long i = 0;
        for (MyTrack track : mTracks) {
            mPlayingQueue.add(new MediaSessionCompat.QueueItem(track.getMediaMetadata().getDescription(), i));
            i++;
        }
        mSession.setQueue(mPlayingQueue);
        mSession.setActive(true);
        mSession.setMetadata(getCurrentSong().getMediaMetadata());
    }

    public boolean isFirstSong() {
        return mTrackPosition == 0;
    }

    public boolean isLastSong() {
        return mTrackPosition == mTracks.size() - 1;
    }

    public Artist getArtist() {
        return mArtist;
    }

    public MyTrack getCurrentSong() {
        return mTracks.get(mTrackPosition);
    }

    public void setShowNotification(boolean showNotification) {
        mShowNotification = showNotification;
        if (mShowNotification) {
            if (mPlayback != null && mPlayback.getState() == PlaybackStateCompat.STATE_PLAYING) {
                mMediaNotificationManager.startNotification();
            }
        } else {
            mMediaNotificationManager.stopNotification();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mPlayerBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    private void sendSongChangedNotification(int trackPosition) {
        Intent i = new Intent(MUSIC_PLAYER_SONG_CHANGED_NOTIFICATION);
        i.putExtra(SONG_POSITION_EXTRA, trackPosition);
        i.putExtra(ARTIST_EXTRA, mArtist);
        broadcastNotification(i);
    }

    private void broadcastNotification(Intent i) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    /**
     * Update the current media player state, optionally showing an error message.
     * <p/>
     * This method was inspired from the sample UniversalMusicPlayer code
     * https://github.com/googlesamples/android-UniversalMusicPlayer
     *
     * @param error if not null, error message to present to the user.
     */
    private void updatePlaybackState(String error) {
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null) {
            position = mPlayback.getCurrentStreamPosition();
        }

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

        @PlaybackStateCompat.State int state = mPlayback.getState();

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }

        stateBuilder.setState(state, position, 1.0f);
        stateBuilder.setActiveQueueItemId(1);

        PlaybackStateCompat playbackState = stateBuilder.build();
        mSession.setPlaybackState(playbackState);
        if (mShowNotification) {
            mMediaNotificationManager.startNotification();
        }
    }

    private void updateMetadata() {
        MediaMetadataCompat track = getCurrentSong().getMediaMetadata();
        mSession.setMetadata(track);
    }

    private
    @PlaybackStateCompat.Actions
    long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY;
        if (mTracks == null || mTracks.isEmpty()) {
            return actions;
        }

        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        if (!isFirstSong()) {
            actions |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        }
        if (!isLastSong()) {
            actions |= PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        }
        return actions;
    }

    @Override
    public void onCompletion() {
        handleSkipToNextSongRequest();
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }

    @Override
    public void onMetadataChanged() {
        updateMetadata();
    }

    private void handlingPlayRequest() {
        mPlayback.play(getCurrentSong());
        // call startService in order to keep the music service running
        startService(new Intent(getApplicationContext(), PlayerService.class));
    }

    private void handleSkipToNextSongRequest() {
        if (!isLastSong()) {
            mTrackPosition++;
            if (mPlayback.getState() == PlaybackStateCompat.STATE_PLAYING) {
                mPlayback.play(getCurrentSong());
            }
            updateMetadata();
            sendSongChangedNotification(mTrackPosition);
        }
    }

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    private final class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            handlingPlayRequest();
        }

        @Override
        public void onPause() {
            mPlayback.pause();
        }

        @Override
        public void onSkipToNext() {
            handleSkipToNextSongRequest();
        }

        @Override
        public void onSkipToPrevious() {
            if (!isFirstSong()) {
                mTrackPosition--;
                if (mPlayback.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    mPlayback.play(getCurrentSong());
                }
                updateMetadata();
                sendSongChangedNotification(mTrackPosition);
            }
        }

        @Override
        public void onSeekTo(long pos) {
            mPlayback.seekTo((int) pos); // this cast is ok as the scrub bar's position is int too
        }
    }
}
