package com.example.android.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.spotifystreamer.adapter.ArtistsAdapter;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;



/**
 * Fragment for searching artists.
 */
public class ArtistSearchActivityFragment extends Fragment {

    ArtistsAdapter mArtistsAdapter;

    public ArtistSearchActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_search, container, false);

        mArtistsAdapter = new ArtistsAdapter(getActivity(), new ArrayList<Artist>());

        // Get a reference to the ListView, and attach the adapter
        ListView listView = (ListView) rootView.findViewById(R.id.list_view_artist_search);
        listView.setAdapter(mArtistsAdapter);

        // Set up the searchView
        EditText searchView = (EditText) rootView.findViewById(R.id.search_artist_editText);
        searchView.requestFocus();
        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean consumed = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    new SearchArtistTask().execute(textView.getText().toString());

                    // By setting consumed to false the soft-keyboard will be hidden
                    consumed = false;
                }
                return consumed;
            }
        });

        return rootView;
    }

    private class SearchArtistTask extends AsyncTask<String, Void, List<Artist>> {
        @Override
        protected List<Artist> doInBackground(String... params) {

            // Query Spotify server
            SpotifyApi api = new SpotifyApi();
            SpotifyService service = api.getService();

            // TODO try ... catch for error handling NullPointerException, etc...
            ArtistsPager results = service.searchArtists(params[0]);
            return results.artists.items;
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            mArtistsAdapter.clear();
            mArtistsAdapter.addAll(artists);
        }
    }
}
