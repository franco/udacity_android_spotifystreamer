/*
 * Copyright (c) 2015 Franco Sebregondi.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
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
