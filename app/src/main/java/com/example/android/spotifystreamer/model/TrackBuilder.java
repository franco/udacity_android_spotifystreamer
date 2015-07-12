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
