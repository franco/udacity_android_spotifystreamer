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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable POJO representing a track.
 */
public class MyTrack implements Parcelable {

    public String trackName;
    public String albumName;
    public String thumbnailUrl;
    public String vignettelUrl;
    public String previewUrl;

    public MyTrack(String trackName, String albumName, String thumbnailUrl, String vignettelUrl,
                   String previewUrl) {
        this.trackName = trackName;
        this.albumName = albumName;
        this.thumbnailUrl = thumbnailUrl;
        this.vignettelUrl = vignettelUrl;
        this.previewUrl = previewUrl;
    }

    private MyTrack(Parcel in) {
        this.trackName = in.readString();
        this.albumName = in.readString();
        this.thumbnailUrl = in.readString();
        this.vignettelUrl = in.readString();
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
        parcel.writeString(vignettelUrl);
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

}
