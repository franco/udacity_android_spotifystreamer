/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer.model;

import android.content.Context;

import com.example.android.spotifystreamer.R;
import com.example.android.spotifystreamer.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Builder class to create MyTrack model object from Spotify Track.
 */
public class TrackBuilder {

    private static int VIGNETTE_SIZE = 640;

    private int mThumbnailSize;

    public TrackBuilder(Context context) {
        // thumbnail_size in pixels (getDimension converts dp to px)
        mThumbnailSize = (int) context.getResources().getDimension(R.dimen.thumbnail_size);
    }

    public MyTrack fromSpotifyTrack(Track spotifyTrack) {

        // Choose an image that is as close as possible to the target dimensions it is going
        // to be displayed (in order to reduce download size).

        Image thumbnail = ImageUtils.findImageWithClosestSize(spotifyTrack.album.images, mThumbnailSize);

        String thumbnailUrl = null;

        if (thumbnail != null) {
            thumbnailUrl = thumbnail.url;
        }

        return new MyTrack(
                spotifyTrack.name,
                spotifyTrack.album.name,
                thumbnailUrl,
                null,
                spotifyTrack.preview_url);
    }

    public ArrayList<MyTrack> fromSpotifyTracks(List<Track> spotifyTracks) {

        ArrayList<MyTrack> tracks = new ArrayList<>();
        for (Track spotifyTrack : spotifyTracks) {
            tracks.add(fromSpotifyTrack(spotifyTrack));
        }
        return tracks;
    }
}
