package com.example.android.spotifystreamer;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistSearchActivityFragment extends Fragment {

    ArrayAdapter<String> mArtistsAdapter;

    public ArtistSearchActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_search, container, false);

        String[] artistsArray = {
                "Arctic Monkeys",
                "Black Eyed Peas",
                "Coldplay",
                "Coldplay & Lee",
                "Coldplay & Rihanna",
                "Daftpunk",
                "Ed Sheeran",
                "Frankie Goes To Hollywood",
                "Sting"
        };

        List<String> artists = new ArrayList<>(Arrays.asList(artistsArray));

        mArtistsAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.list_item_artist_search_result,
                R.id.artist_search_result_textview,
                artists);

        // Get a reference to the ListView, and attach the adapter
        ListView listView = (ListView) rootView.findViewById(R.id.list_view_artist_search);
        listView.setAdapter(mArtistsAdapter);

        return rootView;
    }
}
