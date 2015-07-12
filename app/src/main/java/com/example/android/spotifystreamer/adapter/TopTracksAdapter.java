package com.example.android.spotifystreamer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.spotifystreamer.R;
import com.example.android.spotifystreamer.model.MyTrack;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * TracksAdapter is a specialized ArrayAdapter<MyTrack> for top_tracks ListView.
 */
public class TopTracksAdapter extends ArrayAdapter<MyTrack> {

    private int mThumbnailSize;

    public TopTracksAdapter(Context context, List<MyTrack> tracks) {
        super(context, 0, tracks);

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
                    R.layout.list_item_track,
                    parent,
                    false);

            // Apply ViewHolder pattern according to
            // http://developer.android.com/training/improving-layouts/smooth-scrolling.html
            holder = new ViewHolder();
            holder.thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            holder.album = (TextView) view.findViewById(R.id.album_textView);
            holder.track = (TextView) view.findViewById(R.id.track_textView);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the data item for this position
        MyTrack track = getItem(position);

        // Populate data
        holder.album.setText(track.albumName);
        holder.track.setText(track.trackName);

        if (track.hasThumbnailUrl()) {
            Picasso.with(getContext()).load(track.thumbnailUrl)
                    .resize(mThumbnailSize, mThumbnailSize)
                    .centerInside()
                    .into(holder.thumbnail);
        }

        return view;
    }

    private static class ViewHolder {
        public ImageView thumbnail;
        public TextView album;
        public TextView track;
    }
}
