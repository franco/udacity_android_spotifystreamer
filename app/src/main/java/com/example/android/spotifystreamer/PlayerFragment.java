/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.android.spotifystreamer.model.Artist;
import com.example.android.spotifystreamer.model.MyTrack;
import com.example.android.spotifystreamer.service.PlayerService;
import com.example.android.spotifystreamer.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * DialogFragment that holds the player widget.
 *
 * The design was heavily influenced by the UniversalMusicPlayer sample code. E.g. Scrub bar
 * update was copied from there.
 */
public class PlayerFragment extends DialogFragment {

    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    private PlayerService mPlayerService;
    private boolean mPlayerBound;
    private View mView;
    private Controls mControls = new Controls();
    private MediaControllerCompat mMediaController;
    private PlaybackStateCompat mLastPlaybackState;
    private ScrubBarUpdater mScrubBarUpdater;

    // Callback that ensures that we are showing the controls
    private final MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback(){
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            updatePlaybackState(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata != null) {
                updateMediaDescription();
            }
        }
    };

    public PlayerFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();

        // Bind to PlayerService
        Intent playIntent = new Intent(getActivity(), PlayerService.class);
        getActivity().bindService(playIntent, mPlayerConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mPlayerBound) {
            mMediaController.unregisterCallback(mCallback);
            getActivity().unbindService(mPlayerConnection);
            mPlayerBound = false;
        }
        mScrubBarUpdater.stopSeekbarUpdate();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScrubBarUpdater = new ScrubBarUpdater();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        mView = rootView;

        mControls.bindToView(rootView);
        mControls.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaController.getTransportControls().play();
            }

        });
        mControls.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaController.getTransportControls().skipToNext();
            }
        });
        mControls.prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaController.getTransportControls().skipToPrevious();
            }
        });
        mControls.pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaController.getTransportControls().pause();
            }
        });
        mControls.scrubBar.setMax(MyTrack.PREVIEW_DURATION);
        mControls.scrubBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mControls.currentPositionTextView.setText(Utils.formatScrubBarTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mScrubBarUpdater.stopSeekbarUpdate(); // pause music while scrubbing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMediaController.getTransportControls().seekTo(seekBar.getProgress());
                mScrubBarUpdater.scheduleSeekbarUpdate();
            }
        });

        return rootView;
    }

    /** The system calls this only when creating the layout in a dialog. */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private void updateMediaDescription() {
        View rootView = mView;
        MyTrack song = mPlayerService.getCurrentSong();
        Artist artist = mPlayerService.getArtist();

        TextView artistNameView = (TextView) rootView.findViewById(R.id.artist_name);
        artistNameView.setText(artist.name);
        TextView albumNameView = (TextView) rootView.findViewById(R.id.album_name);
        albumNameView.setText(song.albumName);
        ImageView artworkView = (ImageView) rootView.findViewById(R.id.album_artwork);
        if (song.hasArtworkUrl()) {
            Picasso.with(getActivity()).load(song.artworkUrl)
                    .resize(MyTrack.ARTWORK_SIZE, MyTrack.ARTWORK_SIZE)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image)
                    .into(artworkView);
        }
        TextView trackNameView = (TextView) rootView.findViewById(R.id.track_name);
        trackNameView.setText(song.trackName);
        mControls.scrubBar.setProgress(0);
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state == null) {
            return;
        }
        mLastPlaybackState = state;

        switch (state.getState()) {
            case PlaybackState.STATE_PLAYING:
                mControls.hideLoadingIndicator();
                mControls.showPauseButton();
                mScrubBarUpdater.scheduleSeekbarUpdate();
                break;
            case PlaybackState.STATE_PAUSED:
                mControls.showPlayButton();
                mScrubBarUpdater.stopSeekbarUpdate();
                break;
            case PlaybackState.STATE_NONE:
            case PlaybackState.STATE_STOPPED:
                mControls.showPlayButton();
                mScrubBarUpdater.stopSeekbarUpdate();
                break;
            case PlaybackState.STATE_BUFFERING:
                mControls.showLoadingIndicator();
                mScrubBarUpdater.stopSeekbarUpdate();
                break;
        }

        mControls.prevButton.setEnabled(!mPlayerService.isFirstSong());
        mControls.nextButton.setEnabled(!mPlayerService.isLastSong());
    }

    private ServiceConnection mPlayerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) iBinder;
            mPlayerService = binder.getService();
            mPlayerBound = true;
            updateMediaDescription();
            connectToMediaSession(mPlayerService.getSessionToken());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mPlayerBound = false;
        }
    };

    private void connectToMediaSession(MediaSessionCompat.Token token) {
        try {
            mMediaController = new MediaControllerCompat(getActivity(), token);
            mMediaController.registerCallback(mCallback);
            updatePlaybackState(mMediaController.getPlaybackState());
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Cannot access session");
        }
    }

    /**
     * This code is directly copied from the UniversalAppPlayer sample app.
     */
    private class ScrubBarUpdater {
        private static final long PROGRESS_UPDATE_INTERNAL = 1000;
        private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 1;

        private final Handler mHandler = new Handler();

        private final Runnable mUpdateProgressTask = new Runnable() {
            @Override
            public void run() {
                updateProgress();
            }
        };

        private ScheduledFuture<?> mScheduleFuture;

        private final ScheduledExecutorService mExecutorService =
                Executors.newSingleThreadScheduledExecutor();

        public void scheduleSeekbarUpdate() {
            stopSeekbarUpdate();
            if (!mExecutorService.isShutdown()) {
                mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                        new Runnable() {
                            @Override
                            public void run() {
                                mHandler.post(mUpdateProgressTask);
                            }
                        }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                        PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
            }
        }

        public void stopSeekbarUpdate() {
            if (mScheduleFuture != null) {
                mScheduleFuture.cancel(false);
            }
        }

        private void updateProgress() {
            if (mLastPlaybackState == null) {
                return;
            }

            long currentPosition = mLastPlaybackState.getPosition();

            if (mLastPlaybackState.getState() != PlaybackStateCompat.STATE_PAUSED) {
                // Calculate the elapsed time between the last position update and now and unless
                // paused, we can assume (delta * speed) + current position is approximately the
                // latest position. This ensure that we do not repeatedly call the getPlaybackState()
                // on MediaController.
                long timeDelta = SystemClock.elapsedRealtime() -
                        mLastPlaybackState.getLastPositionUpdateTime();
                currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
            }
            mControls.scrubBar.setProgress((int) currentPosition);
        }
    }

    private class Controls {
        public ImageButton playButton;
        public ImageButton prevButton;
        public ImageButton nextButton;
        public ImageButton pauseButton;
        public SeekBar scrubBar;
        public TextView downloadIndicatorTextView;
        public TextView currentPositionTextView;
        public ProgressBar loadingIndicator;

        public void showPlayButton() {
            pauseButton.setVisibility(View.GONE);
            playButton.setVisibility(View.VISIBLE);
        }

        private void showPauseButton() {
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        }

        public void showLoadingIndicator() {
            mControls.loadingIndicator.setVisibility(View.VISIBLE);
            downloadIndicatorTextView.setVisibility(View.VISIBLE);
        }

        public void hideLoadingIndicator() {
            mControls.loadingIndicator.setVisibility(View.INVISIBLE);
            downloadIndicatorTextView.setVisibility(View.INVISIBLE);
        }

        public void bindToView(View view) {
            playButton = (ImageButton) view.findViewById(R.id.play_button);
            nextButton = (ImageButton) view.findViewById(R.id.next_button);
            prevButton = (ImageButton) view.findViewById(R.id.prev_button);
            pauseButton = (ImageButton) view.findViewById(R.id.pause_button);
            scrubBar = (SeekBar) view.findViewById(R.id.seekBar);
            currentPositionTextView = (TextView) view.findViewById(R.id.track_current_position);
            downloadIndicatorTextView =
                    (TextView) view.findViewById(R.id.track_downloading_indicator);
            loadingIndicator = (ProgressBar) view.findViewById(R.id.loadingIndicator);
        }
    }
}
