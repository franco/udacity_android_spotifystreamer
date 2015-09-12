/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.os.PowerManager;
import android.support.v4.media.session.PlaybackStateCompat;

import com.example.android.spotifystreamer.model.MyTrack;
import com.example.android.spotifystreamer.service.PlayerService;

import java.io.IOException;

/**
 * This class handles the actual playback of a song.
 *
 * This class is heavly inspired by the LocalPlayback class of UniversalMusicPlayer sample code.
 */
public class Playback implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {

    private MediaPlayer mMediaPlayer;
    private PlayerService mService;
    private MyTrack mCurrentTrack;

    private @PlaybackStateCompat.State int mState = PlaybackStateCompat.STATE_NONE;

    public static Callback sDummyCallback = new Callback() {
        @Override
        public void onCompletion() {}

        @Override
        public void onPlaybackStatusChanged(int state) {}

        @Override
        public void onError(String error) {}

        @Override
        public void onMetadataChanged() {}
    };

    private Callback mCallback = sDummyCallback;

    public interface Callback {
        /**
         * On current music completed.
         */
        void onCompletion();
        /**
         * on Playback status changed
         * Implementations can use this callback to update
         * playback state on the media sessions.
         */
        void onPlaybackStatusChanged(int state);

        /**
         * @param error to be added to the PlaybackState
         */
        void onError(String error);

        /**
         * On playing new song
         */
        void onMetadataChanged();
    }

    public Playback(PlayerService service) {
        mService = service;

        // Init MediaPlayer
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setWakeMode(mService.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    @PlaybackStateCompat.State
    public int getState() {
        return mState;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void play(MyTrack track) {
        if (mCurrentTrack == track)  {
            // Song is already loaded
            playSong();
        } else {
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(track.previewUrl);
                mMediaPlayer.prepareAsync();
                mCurrentTrack = track;
                mState = PlaybackStateCompat.STATE_BUFFERING;
                mCallback.onPlaybackStatusChanged(mState);
                mCallback.onMetadataChanged();

            } catch (IOException ioe) {
                mCallback.onError(ioe.getMessage());
            }
        }
    }

    private void playSong() {
        mMediaPlayer.start();
        mState = PlaybackStateCompat.STATE_PLAYING;
        mCallback.onPlaybackStatusChanged(mState);
    }

    public void pause() {
        if (mState == PlaybackStateCompat.STATE_PLAYING) {

            // Pause media player and cancel the 'foreground service' state.
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }

        }
        mState = PlaybackStateCompat.STATE_PAUSED;
        mCallback.onPlaybackStatusChanged(mState);
    }

    public void seekTo(int position) {
        if (mMediaPlayer.isPlaying()) {
            mState = PlaybackState.STATE_BUFFERING;
        }
        mMediaPlayer.seekTo(position);
        mCallback.onPlaybackStatusChanged(mState);
    }

    public int getCurrentStreamPosition() {
        return mMediaPlayer.getCurrentPosition();
    }


    /* MediaPlayer callbacks */

    @Override
    public void onCompletion(MediaPlayer mp) {
        mCallback.onCompletion();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playSong();
    }

    /** Called when MediaPlayer has completed a seek */
    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (mState == PlaybackState.STATE_BUFFERING) {
            mMediaPlayer.start();
            mState = PlaybackState.STATE_PLAYING;
        }
        mCallback.onPlaybackStatusChanged(mState);
    }
}
