package com.example.android.spotifystreamer.model;

import android.content.Context;

import com.example.android.spotifystreamer.R;
import com.example.android.spotifystreamer.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Builder class to create Artist model object from Spotify Artist.
 */
public class ArtistBuilder {

    private int mThumbnailSize;

    public ArtistBuilder(Context context) {
        // thumbnail_size in pixels (getDimension converts dp to px)
        mThumbnailSize = (int) context.getResources().getDimension(R.dimen.thumbnail_size);
    }

    public Artist fromSpotifyArtist(kaaes.spotify.webapi.android.models.Artist spotifyArtist) {

        // Choose an image that is as close as possible to the target dimensions it is going
        // to be displayed (in order to reduce download size).
        Image thumbnail = ImageUtils.findImageWithClosestSize(spotifyArtist.images, mThumbnailSize);

        String thumbnailUrl = null;

        if (thumbnail != null) {
            thumbnailUrl = thumbnail.url;
        }

        return new Artist(spotifyArtist.id, spotifyArtist.name, thumbnailUrl);
    }

    public ArrayList<Artist> fromSpotifyArtists(
            List<kaaes.spotify.webapi.android.models.Artist> spotifyArtists) {

        ArrayList<Artist> artists = new ArrayList<>();
        for (kaaes.spotify.webapi.android.models.Artist spotifyArtist: spotifyArtists) {
            artists.add(fromSpotifyArtist(spotifyArtist));
        }
        return artists;
    }
}
