/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.spotifystreamer.adapter.ArtistsAdapter;
import com.example.android.spotifystreamer.model.Artist;
import com.example.android.spotifystreamer.model.ArtistBuilder;
import com.example.android.spotifystreamer.util.Utils;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;


/**
 * Fragment for searching artists.
 */
public class SearchArtistFragment extends Fragment {

    private static final String LOG_TAG = SearchArtistFragment.class.getSimpleName();
    private static final String STATE_ARTISTS = "state_artists";
    private static final String SELECTED_KEY = "selected_position";

    private ArrayList<Artist> mArtists;
    private ArtistsAdapter mArtistsAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;


    public interface Callback {
        /** Callback for when an item has been selected. */
        void onItemSelected(Artist artist);
    }

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
        mListView = (ListView) rootView.findViewById(R.id.list_view_artist_search);
        mListView.setAdapter(mArtistsAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Artist artist = mArtistsAdapter.getItem(position);
                ((Callback) getActivity())
                        .onItemSelected(artist);
                mPosition = position;
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
                    textView.clearFocus();
                    consumed = true;
                }
                return consumed;
            }
        });

        // Unfortunately the soft-keyboard needs to be hidden manually. See
        // http://stackoverflow.com/a/15413327 and http://stackoverflow.com/a/1662088
        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager)
                            getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        // Restore scroll position
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
            mListView.setSelection(mPosition);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_ARTISTS, mArtists);
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
    }

    private class SearchArtistTask extends AsyncTask<String, Void, ArrayList<Artist>> {

        private ArtistBuilder mArtistBuilder;
        private ProgressDialog mProgressDialog;

        public SearchArtistTask(Context context) {
            mArtistBuilder = new ArtistBuilder(context);
        }

        @Override
        protected ArrayList<Artist> doInBackground(String... params) {
            try {
                // Query Spotify server
                SpotifyApi api = new SpotifyApi();
                SpotifyService service = api.getService();

                ArtistsPager results = service.searchArtists(params[0]);
                return mArtistBuilder.fromSpotifyArtists(results.artists.items);

            } catch (RetrofitError re) {
                Log.e(LOG_TAG, re.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Artist> artists) {
            if (artists != null) {
                mArtists = artists;
                mArtistsAdapter.clear();
                mArtistsAdapter.addAll(mArtists);

                mProgressDialog.dismiss();
                if (artists.size() < 1) {
                    Toast.makeText(
                            getActivity(), R.string.no_artist_found, Toast.LENGTH_SHORT).show();
                }
            } else {
                Utils.showAlertDialog(getActivity(), R.string.spotify_error);
            }
        }

        @Override
        protected void onPreExecute() {
            if (Utils.isNetworkAvailable(getActivity())) {
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
            } else {
                Utils.showAlertDialog(getActivity(), R.string.no_network_error);
                cancel(true);
            }
        }

        @Override
        protected void onCancelled(ArrayList<Artist> artists) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }
}
