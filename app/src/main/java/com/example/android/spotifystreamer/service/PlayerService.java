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

import com.example.android.spotifystreamer.model.Artist;
import com.example.android.spotifystreamer.model.MyTrack;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by franco on 02/08/15.
 */
public class PlayerService extends Service
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public static final String MUSIC_PLAYER_PLAYBACK_STARTED_NOTIFICATION =
            "com.example.android.spotifystreamer.MUSIC_PLAYER_NOTIFICATION.PLAYBACK_STARTED";
    public static final String MUSIC_PLAYER_PREPARING_NOTIFICATION =
            "com.example.android.spotifystreamer.MUSIC_PLAYER_NOTIFICATION.PREPARING";
    public static final String MUSIC_PLAYER_PLAYBACK_STOPPED_NOTIFICATION =
            "com.example.android.spotifystreamer.MUSIC_PLAYER_NOTIFICATION.PLAYBACK_STOPPED";
    public static final String MUSIC_PLAYER_SONG_CHANGED_NOTIFICATION =
            "com.example.android.spotifystreamer.MUSIC_PLAYER_NOTIFICATION.SONG_CHANGED";
    public static final String SONG_POSITION_EXTRA = "song_position_exra";
    public static final String ARTIST_EXTRA = "artist_exra";



    private static final String LOG_TAG = PlayerService.class.getSimpleName();

    private int mTrackPosition;
    private MediaPlayer mMediaPlayer;
    private ArrayList<MyTrack> mTracks;
    private Artist mArtist;
    private MyTrack mCurrentlyLoadedTrack;
    private boolean mIsPrepared;

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
        Log.d(LOG_TAG, "onCreate Service " + this.hashCode());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy Service " + this.hashCode());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // startForeground(notification);

        // reset player
        Log.d(LOG_TAG, "onStartCommand");


        return 0;
    }

    public void playTracks(ArrayList<MyTrack> tracks, int trackPosition, Artist artist) {

        // Only start playing requested song is it is another then the already loaded song.
        if (!tracks.get(trackPosition).equals(mCurrentlyLoadedTrack)) {
            mTracks = tracks;
            mTrackPosition = trackPosition;
            mArtist = artist;
            mIsPrepared = false;
        }

        playSong();
    }

    public void playSong() {
        Log.d(LOG_TAG, "playSong");

        if (mIsPrepared) {
            startSong();
        } else {
            sendDownloadingNotification();

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
        if (!isLastSong()) {
            mTrackPosition++;
            mIsPrepared = false;
            playSong();
            sendSongChangedNotification(mTrackPosition);
        }
    }

    public void playPrevSong() {
        if (!isFirstSong()) {
            mTrackPosition--;
            mIsPrepared = false;
            playSong();
            sendSongChangedNotification(mTrackPosition);

        }
    }

    public void pauseSong() {
        mMediaPlayer.pause();
        sendPlaybackStoppedNotification();
    }

    public void startSong() {
        mMediaPlayer.start();
        sendPlaybackStartedNotification();
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
        return mTrackPosition == mTracks.size() - 1;
    }

    public int getCurrentPosition() {
        return mTrackPosition;
    }

    public ArrayList<MyTrack> getSongs() {
        return mTracks;
    }

    public Artist getArtist() {
        return mArtist;
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
        mIsPrepared = true;
        playSong();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (isLastSong()) {
            sendPlaybackStoppedNotification();
        } else {
            playNextSong();
        }
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

    private void sendSongChangedNotification(int trackPosition) {
        Log.d(LOG_TAG, "send notification: SONG CHANGED");
        Intent i = new Intent(MUSIC_PLAYER_SONG_CHANGED_NOTIFICATION);
        i.putExtra(SONG_POSITION_EXTRA, trackPosition);
        i.putExtra(ARTIST_EXTRA, mArtist);
        broadcastNotification(i);
    }

    private void sendPlaybackStartedNotification() {
        Log.d(LOG_TAG, "send notification: PLAYBACK_STARTED");
        broadcastNotification(new Intent(MUSIC_PLAYER_PLAYBACK_STARTED_NOTIFICATION));
    }

    private void sendPlaybackStoppedNotification() {
        Log.d(LOG_TAG, "send notification: PLAYBACK_STOPPED");
        broadcastNotification(new Intent(MUSIC_PLAYER_PLAYBACK_STOPPED_NOTIFICATION));
    }

    private void sendDownloadingNotification() {
        Log.d(LOG_TAG, "send notification: PREPARING");
        broadcastNotification(new Intent(MUSIC_PLAYER_PREPARING_NOTIFICATION));
    }

    private void broadcastNotification(Intent i) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
}
