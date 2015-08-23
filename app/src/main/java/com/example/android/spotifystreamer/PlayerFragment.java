/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
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
import java.util.List;
import java.util.concurrent.TimeUnit;


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
    private SeekBar mSeekBar;
    private ImageButton mPauseButton;
    private ImageButton mPlayButton;


    public PlayerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (playIntent == null) {
            playIntent = new Intent(getActivity(), PlayerService.class);
            getActivity().bindService(playIntent, playerConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
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

        mPlayButton = (ImageButton) rootView.findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPlaying();
                togglePlayButton();
            }
        });

        ImageButton nextButton = (ImageButton) rootView.findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTrackPosition < mTracks.size()) {
                    mTrackPosition++;
                    propagateDataToView();
                    mPlayerService.playNextSong();
                    showPauseButton();
                    mHandler.post(mProgressBarUpdate);
                }
            }
        });

        ImageButton prevButton = (ImageButton) rootView.findViewById(R.id.prev_button);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTrackPosition > 0) {
                    mTrackPosition--;
                    propagateDataToView();
                    mPlayerService.playPrevSong();
                    showPauseButton();
                    mHandler.post(mProgressBarUpdate);
                }
            }
        });

        mPauseButton = (ImageButton) rootView.findViewById(R.id.pause_button);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pausePlaying();
                togglePlayButton();
            }
        });

        final TextView currentPos = (TextView) rootView.findViewById(R.id.track_current_position);

        mSeekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        mSeekBar.setMax(MyTrack.PREVIEW_DURATION);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Log.d(LOG_TAG, "Seek to " + progress);
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
    public void onPause() {
        super.onPause();

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
        mHandler.post(mProgressBarUpdate);
    }

    private void pausePlaying() {
        mPlayerService.pauseSong();
        mHandler.removeCallbacks(mProgressBarUpdate);
    }

    private MyTrack getCurrentTrack() {
        return mTracks.get(mTrackPosition);
    }

    private void propagateDataToView() {
        View rootView = getView();

        TextView artistNameView = (TextView) rootView.findViewById(R.id.artist_name);
        artistNameView.setText(mArtist.name);
        TextView albumNameView = (TextView) rootView.findViewById(R.id.album_name);
        albumNameView.setText(getCurrentTrack().albumName);
        ImageView artworkView = (ImageView) rootView.findViewById(R.id.album_artwork);
        if (getCurrentTrack().hasArtworkUrl()) {
            Picasso.with(getActivity()).load(getCurrentTrack().artworkUrl)
                    .resize(MyTrack.ARTWORK_SIZE, MyTrack.ARTWORK_SIZE)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image)
                    .into(artworkView);
        }
        TextView trackNameView = (TextView) rootView.findViewById(R.id.track_name);
        trackNameView.setText(getCurrentTrack().trackName);
    }

    private ServiceConnection playerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) iBinder;
            mPlayerService = binder.getService();

            mPlayerService.setTracks(mTracks);
            mPlayerService.setTrackPosition(mTrackPosition);

            isPlayerBound = true;

            if (mPlayerService.isPlaying()) {
                mHandler.post(mProgressBarUpdate);
                showPauseButton();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isPlayerBound = false;
        }
    };

    @Nullable
    @Override
    public View getView() {
        return mView;
    }

    private void togglePlayButton() {
        if (mPlayButton.getVisibility() == View.GONE) {
            showPlayButton();
        } else {
            showPauseButton();
        }
    }

    private void showPlayButton() {
        mPauseButton.setVisibility(View.GONE);
        mPlayButton.setVisibility(View.VISIBLE);
    }

    private void showPauseButton() {
        mPlayButton.setVisibility(View.GONE);
        mPauseButton.setVisibility(View.VISIBLE);
    }


    private Runnable mProgressBarUpdate = new Runnable() {

        @Override
        public void run() {
            if (mPlayerService != null) {
                mSeekBar.setProgress(mPlayerService.getSongProgress());
            }

            if (isPlayerBound) mHandler.postDelayed(this, 1000);
        }
    };
}
