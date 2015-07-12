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

package com.example.android.spotifystreamer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.spotifystreamer.R;
import com.example.android.spotifystreamer.model.Artist;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Class ArtistsAdapter is a specialized ArrayAdapter<Artist> for artist_search_result ListView.
 *
 * Inspired by
 *   - https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
 *   - http://ocddevelopers.com/2014/extend-baseadapter-instead-of-arrayadapter-for-custom-list-items/
 *   - http://developer.android.com/training/improving-layouts/smooth-scrolling.html
 */
public class ArtistsAdapter extends ArrayAdapter<Artist> {

    private int mThumbnailSize;

    public ArtistsAdapter(Context context, List<Artist> artists) {
        super(context, 0, artists);

        // thumbnail_size in pixels (getDimension converts dp to px)
        mThumbnailSize = (int) getContext().getResources().getDimension(R.dimen.thumbnail_size);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item_artist,
                    parent,
                    false);

            // Apply ViewHolder pattern according to
            // http://developer.android.com/training/improving-layouts/smooth-scrolling.html
            holder = new ViewHolder();
            holder.thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            holder.name = (TextView) view.findViewById(R.id.artist_textView);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the data item for this position
        Artist artist = getItem(position);

        // Populate data
        holder.name.setText(artist.name);

        if (artist.hasThumbnailUrl()) {
            Picasso.with(getContext()).load(artist.thumbnailUrl)
                    .resize(mThumbnailSize, mThumbnailSize)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.thumbnail);
        }

        return view;
    }

    private static class ViewHolder {
        public ImageView thumbnail;
        public TextView name;
    }
}
