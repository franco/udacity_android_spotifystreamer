/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable POJO representing an artist.
 */
public class Artist implements Parcelable {
    public String id;
    public String name;
    public String thumbnailUrl;

    public Artist(String id, String name, String thumbnailUrl){
        this.id = id;
        this.name = name;
        this.thumbnailUrl = thumbnailUrl;
    }

    private Artist(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.thumbnailUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(thumbnailUrl);
    }

    public static final Parcelable.Creator<Artist> CREATOR = new Parcelable.Creator<Artist>() {
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    public boolean hasThumbnailUrl() {
        return thumbnailUrl != null;
    }
}
