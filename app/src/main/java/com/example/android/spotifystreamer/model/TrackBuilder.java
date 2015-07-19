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

    private int mThumbnailSize;

    public TrackBuilder(Context context) {
        // thumbnail_size in pixels (getDimension converts dp to px)
        mThumbnailSize = (int) context.getResources().getDimension(R.dimen.thumbnail_size);
    }

    /**
     * Converts a Spotify Track model object to our own parcelable object. As thumbnail
     * it finds the image with the closest dimension to the thumbnail size used in the
     * corresponding ListView.
     */
    public MyTrack fromSpotifyTrack(Track spotifyTrack) {

        // Choose the image from the list of album images provided by spotify which comes closest
        // to the thumbnail size used in the corresponding ListView. This optimizes the download
        // size of the image.
        Image thumbnail =
                ImageUtils.findImageWithClosestSize(spotifyTrack.album.images, mThumbnailSize);
        String thumbnailUrl = null;
        if (thumbnail != null) {
            thumbnailUrl = thumbnail.url;
        }

        // Choose the image from the list of album images which is closest to the artwork size
        // This optimizes the download size of the image.

        Image artwork =
                ImageUtils.findImageWithClosestSize(spotifyTrack.album.images, MyTrack.ARTWORK_SIZE);

        String artworkUrl = null;
        if (artwork != null) {
            artworkUrl = artwork.url;
        }

        return new MyTrack(
                spotifyTrack.name,
                spotifyTrack.album.name,
                thumbnailUrl,
                artworkUrl,
                spotifyTrack.preview_url);
    }

    /** Converts a List with spotify Tracks to a list with our own parcelable Track objects */
    public ArrayList<MyTrack> fromSpotifyTracks(List<Track> spotifyTracks) {

        ArrayList<MyTrack> tracks = new ArrayList<>();
        for (Track spotifyTrack : spotifyTracks) {
            tracks.add(fromSpotifyTrack(spotifyTrack));
        }
        return tracks;
    }
}
