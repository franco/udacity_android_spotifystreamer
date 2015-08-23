/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.android.spotifystreamer.model.MyTrack;

import java.io.IOException;
import java.util.List;

/**
 * Created by franco on 02/08/15.
 */
public class PlayerService extends Service
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public static final String MUSIC_PLAYER_NOTIFICATION =
            "com.example.android.spotifystreamer.MUSIC_PLAYER_NOTIFICATION";
    public static final String MUSIC_PLAYER_NOTIFICATION_EXTRA = "play_notification_extra";
    public static final String MUSIC_PLAYER_ACTION_DOWNLOADING = "downloading";
    public static final String MUSIC_PLAYER_ACTION_PLAYING = "playing";
    public static final String MUSIC_PLAYER_ACTION_STOPPED = "stopped";
    public static final String MUSIC_PLAYER_ACTION_SONG_CHANGED = "song_changed";

    private static final String LOG_TAG = PlayerService.class.getSimpleName();

    private int mTrackPosition;
    private MediaPlayer mMediaPlayer;
    private List<MyTrack> mTracks;
    private MyTrack mCurrentlyLoadedTrack;
    private boolean isPrepared;

    private final IBinder mPlayerBinder = new PlayerBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        mTrackPosition = 0;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        // TODO wakelock and foreground
//            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        Log.d(LOG_TAG, "onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // startForeground(notification);

        // reset player
        Log.d(LOG_TAG, "onStartCommand");


        return 0;
    }

    public void playTracks(List<MyTrack> tracks, int trackPosition) {
        Log.d(LOG_TAG, "playTracks(List<MyTracks>, trackPosition");

        if (tracks.get(trackPosition) == mCurrentlyLoadedTrack) {
            // nothing to do. Just play the
            Log.d(LOG_TAG, "continue play current song");

        } else {
            Log.d(LOG_TAG, "loading new Song");

            mTracks = tracks;
            mTrackPosition = trackPosition;
            isPrepared = false;
        }

        playSong();
    }

    public void playSong() {
        Log.d(LOG_TAG, "playSong");

        if (isPrepared) {
            startSong();
            broadcastPlayerNotification(MUSIC_PLAYER_ACTION_PLAYING);
        } else {
            broadcastPlayerNotification(MUSIC_PLAYER_ACTION_DOWNLOADING);

            mMediaPlayer.reset();

            MyTrack track = mTracks.get(mTrackPosition);

            try {
                mMediaPlayer.setDataSource(track.previewUrl);
            } catch (IOException ioe) {
                //throw new StreamingException(ioe.getMessage());
                // TODO what to do in this case?
            } catch (IllegalArgumentException iae) {
                //            throw new StreamingException(iae.getMessage());
                // TODO what do do in this case
            }

            Log.d(LOG_TAG, "calling prepareAsync... ");
            mMediaPlayer.prepareAsync(); // prepare async to not block main thread
        }
    }

    public void playNextSong() {
        if (mTrackPosition < mTracks.size()) {
            mTrackPosition++;
            isPrepared = false;
            playSong();
            broadcastPlayerNotification(MUSIC_PLAYER_ACTION_SONG_CHANGED);
        }
    }

    public void playPrevSong() {
        if (mTrackPosition > 0) {
            mTrackPosition--;
            isPrepared = false;
            playSong();
            broadcastPlayerNotification(MUSIC_PLAYER_ACTION_SONG_CHANGED);
        }
    }

    public void pauseSong() {
        mMediaPlayer.pause();
        broadcastPlayerNotification(MUSIC_PLAYER_ACTION_STOPPED);
    }

    public void startSong() {
        mMediaPlayer.start();
    }

    public void seekTo(int position) {
        mMediaPlayer.seekTo(position);
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    /** Returns the current position in ms */
    public int getSongProgress() {
        return mMediaPlayer.getCurrentPosition();
    }

    public boolean isFirstSong() {
        return mTrackPosition == 0;
    }
    public boolean isLastSong() {
        return mTrackPosition == mTracks.size();
    }

    public int getCurrentPosition() {
        return mTrackPosition;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mPlayerBinder;
    }

//    @Override
//    public boolean onUnbind(Intent intent) {
//        mMediaPlayer.stop();
//        mMediaPlayer.release();
//        return false;
//    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(LOG_TAG, "onPrepared called.... starting mediaPlayer.start()");
        mCurrentlyLoadedTrack = mTracks.get(mTrackPosition);
        isPrepared = true;
        playSong();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        broadcastPlayerNotification(MUSIC_PLAYER_ACTION_STOPPED);
    }

    public static class StreamingException extends Exception {
        public StreamingException(String detailMessage) {
            super(detailMessage);
        }
    };

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    private void broadcastPlayerNotification(String message) {
        Intent i = new Intent(MUSIC_PLAYER_NOTIFICATION);
        i.putExtra(MUSIC_PLAYER_NOTIFICATION_EXTRA, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
}
