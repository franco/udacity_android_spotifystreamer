/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.spotifystreamer.model.Artist;
import com.example.android.spotifystreamer.model.MyTrack;
import com.squareup.picasso.Picasso;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends Fragment {

    public static final String EXTRA_ARTIST = "artist";
    public static final String EXTRA_TRACK = "track";

    private Artist mArtist;
    private MyTrack mTrack;

    public PlayerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        if (savedInstanceState != null) {
            // TODO

        } else {

            // Extract intent extra
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(EXTRA_ARTIST) && intent.hasExtra(EXTRA_TRACK)) {
                mArtist = intent.getParcelableExtra(EXTRA_ARTIST);
                mTrack = intent.getParcelableExtra(EXTRA_TRACK);
            }
        }

        propagateDataToView(rootView);

        return rootView;
    }

    private void propagateDataToView(View rootView) {
        TextView artistNameView = (TextView) rootView.findViewById(R.id.artist_name);
        artistNameView.setText(mArtist.name);
        TextView albumNameView = (TextView) rootView.findViewById(R.id.album_name);
        albumNameView.setText(mTrack.albumName);
        ImageView artworkView = (ImageView) rootView.findViewById(R.id.album_artwork);
        if (mTrack.hasArtworkUrl()) {
            Picasso.with(getActivity()).load(mTrack.artworkUrl)
                    .resize(MyTrack.ARTWORK_SIZE, MyTrack.ARTWORK_SIZE)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image)
                    .into(artworkView);
        }
        TextView trackNameView = (TextView) rootView.findViewById(R.id.track_name);
        trackNameView.setText(mTrack.trackName);
    }
}
