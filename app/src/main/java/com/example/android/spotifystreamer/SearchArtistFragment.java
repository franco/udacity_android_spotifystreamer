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

package com.example.android.spotifystreamer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.spotifystreamer.adapter.ArtistsAdapter;
import com.example.android.spotifystreamer.model.Artist;
import com.example.android.spotifystreamer.model.ArtistBuilder;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * Fragment for searching artists.
 */
public class SearchArtistFragment extends Fragment {

    private static final String STATE_ARTISTS = "state_artists";

    ArrayList<Artist> mArtists;
    ArtistsAdapter mArtistsAdapter;

    public SearchArtistFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_artist, container, false);

        if (savedInstanceState != null) {
            mArtists = savedInstanceState.getParcelableArrayList(STATE_ARTISTS);
        } else {
            mArtists = new ArrayList<>();
        }

        mArtistsAdapter = new ArtistsAdapter(getActivity(), mArtists);

        // Get a reference to the ListView, attach the adapter, and set onItemClickListener
        ListView listView = (ListView) rootView.findViewById(R.id.list_view_artist_search);
        listView.setAdapter(mArtistsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Artist artist = mArtistsAdapter.getItem(i);
                Intent intent = new Intent(getActivity(), TopTracksActivity.class);
                intent.putExtra(TopTracksFragment.EXTRA_ARTIST, artist);
                startActivity(intent);
            }
        });

        // Set up the searchView
        EditText searchView = (EditText) rootView.findViewById(R.id.search_artist_editText);
        searchView.requestFocus();
        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean consumed = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    new SearchArtistTask(getActivity()).execute(textView.getText().toString());

                    // By setting consumed to false the soft-keyboard will be hidden
                    consumed = false;
                }
                return consumed;
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_ARTISTS, mArtists);
    }

    private class SearchArtistTask extends AsyncTask<String, Void, ArrayList<Artist>> {

        private ArtistBuilder mArtistBuilder;
        private ProgressDialog mProgressDialog;


        public SearchArtistTask(Context context) {
            mArtistBuilder = new ArtistBuilder(context);
        }

        @Override
        protected ArrayList<Artist> doInBackground(String... params) {

            // Query Spotify server
            SpotifyApi api = new SpotifyApi();
            SpotifyService service = api.getService();

            // TODO try ... catch for error handling NullPointerException, etc...
            ArtistsPager results = service.searchArtists(params[0]);

            return mArtistBuilder.fromSpotifyArtists(results.artists.items);
        }

        @Override
        protected void onPostExecute(ArrayList<Artist> artists) {
            mArtists = artists;
            mArtistsAdapter.clear();
            mArtistsAdapter.addAll(mArtists);

            mProgressDialog.dismiss();
            if (artists.size() < 1) {
                Toast.makeText(getActivity(), R.string.no_artist_found, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(getActivity(),
                    null,
                    getResources().getString(R.string.loading_text),
                    true,
                    true);

            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                    mProgressDialog.cancel();
                }
            });
        }

        @Override
        protected void onCancelled(ArrayList<Artist> artists) {
            mProgressDialog.dismiss();
        }
    }
}
