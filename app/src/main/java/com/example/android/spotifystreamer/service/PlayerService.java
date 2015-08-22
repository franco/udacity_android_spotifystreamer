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

import com.example.android.spotifystreamer.model.MyTrack;

import java.io.IOException;
import java.util.List;

/**
 * Created by franco on 02/08/15.
 */
public class PlayerService extends Service
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener{

    public static final String ACTION_PLAY = "com.example.android.spotifystreamer.action.PLAY";
    public static final String URL_EXTRA = "url";


    private String mUrl;
    private int mTrackPosition;
    private MediaPlayer mMediaPlayer;
    private List<MyTrack> mTracks;
    private boolean isPrepared;

    private final IBinder mPlayerBinder = new PlayerBinder();


    @Override
    public void onCreate() {
        super.onCreate();
        mTrackPosition = 0;
        mMediaPlayer = new MediaPlayer();
        initMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // startForeground(notification);

//        if (intent.getAction().equals(ACTION_PLAY)) {
//            mUrl = intent.getStringExtra(URL_EXTRA);
//            initMediaPlayer();
//        }


        return 0;

    }

    /** Initializes MediaPlayer */
    public void initMediaPlayer() {
        // TODO wakelock and foreground


//            mMediaPlayer = new MediaPlayer();
//            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mMediaPlayer.setDataSource(mUrl);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnPreparedListener(this);
//            mMediaPlayer.prepareAsync(); // prepare async to not block main thread

    }

    public void setTracks(List<MyTrack> tracks) {
        mTracks = tracks;
    }

    public void setTrackPosition(int trackPosition) {
        mTrackPosition = trackPosition;
    }

    public void playSong() {

        if (isPrepared) {
            startSong();
        } else {

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

            mMediaPlayer.prepareAsync(); // prepare async to not block main thread
        }
    }

    public void playNextSong() {
        if (mTrackPosition < mTracks.size()) {
            mTrackPosition++;
            isPrepared = false;
            playSong();
        }
    }

    public void playPrevSong() {
        if (mTrackPosition > 0) {
            mTrackPosition--;
            isPrepared = false;
            playSong();
        }
    }

    public void pauseSong() {
        mMediaPlayer.pause();
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

    /** Returns the current postion in ms */
    public int getSongProgress() {
        return mMediaPlayer.getCurrentPosition();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mPlayerBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        isPrepared = true;
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
}
