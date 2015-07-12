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

/**
 * Builder class to create MyArtist model object from Spotify Artist.
 */
public class ArtistBuilder {

    private int mThumbnailSize;

    public ArtistBuilder(Context context) {
        // thumbnail_size in pixels (getDimension converts dp to px)
        mThumbnailSize = (int) context.getResources().getDimension(R.dimen.thumbnail_size);
    }

    /**
     * Converts a Spotify Artist model object to our own parcelable object. As thumbnail
     * it finds the image with the closest dimension to the thumbnail size used in the
     * corresponding ListView.
     */
    public Artist fromSpotifyArtist(kaaes.spotify.webapi.android.models.Artist spotifyArtist) {

        // Choose the image from the list of album images provided by spotify which comes closest
        // to the thumbnail size used in the corresponding ListView. This optimizes the download
        // size of the image.
        Image thumbnail = ImageUtils.findImageWithClosestSize(spotifyArtist.images, mThumbnailSize);

        String thumbnailUrl = null;
        if (thumbnail != null) {
            thumbnailUrl = thumbnail.url;
        }

        return new Artist(spotifyArtist.id, spotifyArtist.name, thumbnailUrl);
    }

    /** Converts a List with spotify Tracks to a list with our own parcelable Track objects */
    public ArrayList<Artist> fromSpotifyArtists(
            List<kaaes.spotify.webapi.android.models.Artist> spotifyArtists) {

        ArrayList<Artist> artists = new ArrayList<>();
        for (kaaes.spotify.webapi.android.models.Artist spotifyArtist: spotifyArtists) {
            artists.add(fromSpotifyArtist(spotifyArtist));
        }
        return artists;
    }
}
