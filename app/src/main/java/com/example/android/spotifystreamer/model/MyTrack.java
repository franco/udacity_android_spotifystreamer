/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable POJO representing a track.
 */
public class MyTrack implements Parcelable {

    public static final int ARTWORK_SIZE = 640;
    public static final int PREVIEW_DURATION = 30000; // in ms

    public String trackName;
    public String albumName;
    public String thumbnailUrl;
    public String artworkUrl;
    public String previewUrl;

    public MyTrack(String trackName, String albumName, String thumbnailUrl, String artworklUrl,
                   String previewUrl) {
        this.trackName = trackName;
        this.albumName = albumName;
        this.thumbnailUrl = thumbnailUrl;
        this.artworkUrl = artworklUrl;
        this.previewUrl = previewUrl;
    }

    private MyTrack(Parcel in) {
        this.trackName = in.readString();
        this.albumName = in.readString();
        this.thumbnailUrl = in.readString();
        this.artworkUrl = in.readString();
        this.previewUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(trackName);
        parcel.writeString(albumName);
        parcel.writeString(thumbnailUrl);
        parcel.writeString(artworkUrl);
        parcel.writeString(previewUrl);
    }

    public static final Parcelable.Creator<MyTrack> CREATOR = new Parcelable.Creator<MyTrack>() {
        public MyTrack createFromParcel(Parcel in) {
            return new MyTrack(in);
        }

        public MyTrack[] newArray(int size) {
            return new MyTrack[size];
        }
    };

    public boolean hasThumbnailUrl() {
        return thumbnailUrl != null;
    }

    public boolean hasArtworkUrl() {
        return artworkUrl != null;
    }

}
