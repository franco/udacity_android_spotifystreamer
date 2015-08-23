/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.android.spotifystreamer.model.Artist;
import com.example.android.spotifystreamer.model.MyTrack;
import com.example.android.spotifystreamer.service.PlayerService;
import com.example.android.spotifystreamer.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * DialogFragment that holds the player.
 */
public class PlayerFragment extends DialogFragment {

    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    public static final String EXTRA_ARTIST = "artist";
    public static final String EXTRA_TRACKS = "tracks";
    public static final String EXTRA_CURRENT_TRACK_POSITION = "ctp";
    private static final String STATE_ARTIST = "state_artist";
    private static final String STATE_TRACKS = "state_tracks";
    private static final String STATE_POSITION = "state_position";

    private Artist mArtist;
    private ArrayList<MyTrack> mTracks;
    private int mTrackPosition;

    private PlayerService mPlayerService;
    private boolean isPlayerBound;
    private Intent playIntent;
    private final Handler mHandler = new Handler();

    private View mView;

    private Controls mControls = new Controls();

    public PlayerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (playIntent == null) {
            playIntent = new Intent(getActivity(), PlayerService.class);
            getActivity().bindService(playIntent, playerConnection, Context.BIND_AUTO_CREATE);

            if (savedInstanceState == null) {
                getActivity().startService(playIntent);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mOnMusicPlayerChangedNotification,
                new IntentFilter(PlayerService.MUSIC_PLAYER_NOTIFICATION));
    }

    @Override
    public void onPause() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mOnMusicPlayerChangedNotification);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        mView = rootView;

        if (savedInstanceState != null) {
            mArtist = savedInstanceState.getParcelable(STATE_ARTIST);
            mTracks = savedInstanceState.getParcelableArrayList(STATE_TRACKS);
            mTrackPosition = savedInstanceState.getInt(STATE_POSITION);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mArtist = arguments.getParcelable(PlayerFragment.EXTRA_ARTIST);
                mTracks = arguments.getParcelableArrayList(PlayerFragment.EXTRA_TRACKS);
                mTrackPosition = arguments.getInt(PlayerFragment.EXTRA_CURRENT_TRACK_POSITION);
            }
        }

        propagateDataToView();

        mControls.playButton = (ImageButton) rootView.findViewById(R.id.play_button);
        mControls.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPlaying();
            }
        });

        mControls.nextButton = (ImageButton) rootView.findViewById(R.id.next_button);
        mControls.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayerService.playNextSong();
            }
        });

        mControls.prevButton = (ImageButton) rootView.findViewById(R.id.prev_button);
        mControls.prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayerService.playPrevSong();
            }
        });

        mControls.pauseButton = (ImageButton) rootView.findViewById(R.id.pause_button);
        mControls.pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pausePlaying();
            }
        });

        final TextView currentPos = (TextView) rootView.findViewById(R.id.track_current_position);

        mControls.scrubBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        mControls.scrubBar.setMax(MyTrack.PREVIEW_DURATION);
        mControls.scrubBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mPlayerService.seekTo(progress);
                }
                currentPos.setText(Utils.formatScrubBarTime(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // pause music while scrubbing
                mPlayerService.pauseSong();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPlayerService.startSong();
            }
        });

        return rootView;
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_ARTIST, mArtist);
        outState.putParcelableArrayList(STATE_TRACKS, mTracks);
        outState.putInt(STATE_POSITION, mTrackPosition);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isPlayerBound) {
            getActivity().unbindService(playerConnection);
            isPlayerBound = false;
        }
    }

    private void startPlaying() {
        mPlayerService.playSong();
    }

    private void pausePlaying() {
        mPlayerService.pauseSong();
    }

    private void propagateDataToView() {
        View rootView = mView;
        MyTrack song = mTracks.get(mTrackPosition);

        TextView artistNameView = (TextView) rootView.findViewById(R.id.artist_name);
        artistNameView.setText(mArtist.name);
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
    }

    private ServiceConnection playerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) iBinder;
            mPlayerService = binder.getService();

            mPlayerService.playTracks(mTracks, mTrackPosition);
            isPlayerBound = true;

            if (mPlayerService.isPlaying()) {
                mControls.registerProgressBarUpdate();
                mControls.showPauseButton();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isPlayerBound = false;
        }
    };

    private BroadcastReceiver mOnMusicPlayerChangedNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(PlayerService.MUSIC_PLAYER_NOTIFICATION_EXTRA);

            switch (message) {
                case PlayerService.MUSIC_PLAYER_ACTION_DOWNLOADING:
                    mControls.showPauseButton();
                    break;
                case PlayerService.MUSIC_PLAYER_ACTION_PLAYING:
                    mControls.registerProgressBarUpdate();
                    mControls.showPauseButton();
                    break;
                case PlayerService.MUSIC_PLAYER_ACTION_STOPPED:
                    mControls.unregisterProgressBarUpdate();
                    mControls.showPlayButton();
                    break;
                case PlayerService.MUSIC_PLAYER_ACTION_SONG_CHANGED:
                    mTrackPosition = mPlayerService.getCurrentPosition();
                    propagateDataToView();
                    mControls.resetControls();
                    break;
            }
        }
    };


    private class Controls {
        public ImageButton playButton;
        public ImageButton prevButton;
        public ImageButton nextButton;
        public ImageButton pauseButton;
        public SeekBar scrubBar;

        private Runnable mProgressBarUpdate = new Runnable() {
            @Override
            public void run() {
                if (mPlayerService != null) {
                    scrubBar.setProgress(mPlayerService.getSongProgress());
                }
                if (isPlayerBound) mHandler.postDelayed(this, 1000);
            }
        };

        public void resetControls() {
            if (isPlayerBound) {
                prevButton.setEnabled(!mPlayerService.isFirstSong());
                nextButton.setEnabled(!mPlayerService.isLastSong());
            }
        }

        public void showPlayButton() {
            pauseButton.setVisibility(View.GONE);
            playButton.setVisibility(View.VISIBLE);
        }

        private void showPauseButton() {
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        }


        public void registerProgressBarUpdate() {
            Log.d(LOG_TAG, "registerProgressBarUpdate");
            mHandler.post(mProgressBarUpdate);
        }

        public void unregisterProgressBarUpdate() {
            Log.d(LOG_TAG, "unregisterProgressBarUpdate");
            mHandler.removeCallbacks(mProgressBarUpdate);
        }
    }
}
