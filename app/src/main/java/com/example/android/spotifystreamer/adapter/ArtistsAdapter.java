package com.example.android.spotifystreamer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.spotifystreamer.R;
import com.example.android.spotifystreamer.util.ImageUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

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
                    R.layout.list_item_artist_search_result,
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

        // Choose an image that is as close as possible to the target dimensions it is going
        // to be displayed (in order to reduce download size).
        Image thumbnail = ImageUtils.findImageWithClosestSize(artist.images, mThumbnailSize);

        if (thumbnail != null) {
           Picasso.with(getContext()).load(thumbnail.url)
                   .resize(mThumbnailSize, mThumbnailSize)
                    .centerInside()
                    .into(holder.thumbnail);
        }

        return view;
    }

    private static class ViewHolder {
        public ImageView thumbnail;
        public TextView name;
    }
}
