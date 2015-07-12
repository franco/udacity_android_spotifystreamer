package com.example.android.spotifystreamer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.spotifystreamer.adapter.TopTracksAdapter;
import com.example.android.spotifystreamer.model.Artist;
import com.example.android.spotifystreamer.model.MyTrack;
import com.example.android.spotifystreamer.model.TrackBuilder;
import com.example.android.spotifystreamer.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksFragment extends Fragment {

    public static final String EXTRA_ARTIST = "artist";
    private static final String STATE_ARTIST = "state_artist" ;
    private static final String STATE_TRACKS = "state_tracks";


    private Artist mArtist;
    private ArrayList<MyTrack> mTopTracks;
    private TopTracksAdapter mTopTracksAdapter;

    public TopTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        if (savedInstanceState != null) {
            mArtist = savedInstanceState.getParcelable(STATE_ARTIST);
            mTopTracks = savedInstanceState.getParcelableArrayList(STATE_TRACKS);
        } else {
            // Extract intent extra
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(EXTRA_ARTIST)) {
                mArtist = intent.getParcelableExtra(EXTRA_ARTIST);
                new SearchTrackTask(getActivity()).execute(mArtist.id);
            }
            mTopTracks = new ArrayList<>();
        }

        mTopTracksAdapter = new TopTracksAdapter(getActivity(), mTopTracks);

        // Set action bar subtitle
        ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(mArtist.name);

        // Get a reference to the ListView, and attach the adapter
        ListView listView = (ListView) rootView.findViewById(R.id.list_view_top_tracks);
        listView.setAdapter(mTopTracksAdapter);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_ARTIST, mArtist);
        outState.putParcelableArrayList(STATE_TRACKS, mTopTracks);
    }

    private class SearchTrackTask extends AsyncTask<String, Void, ArrayList<MyTrack>> {

        private TrackBuilder mTrackBuilder;
        private ProgressDialog mProgressDialog;

        public SearchTrackTask(Context context) {
            mTrackBuilder = new TrackBuilder(context);
        }

        @Override
        protected ArrayList<MyTrack> doInBackground(String... params) {

            // Query Spotify server
            SpotifyApi api = new SpotifyApi();
            SpotifyService service = api.getService();


            // TODO try ... catch for error handling NullPointerException, etc...
            Map<String, Object> options = new HashMap<>();
            options.put(SpotifyService.COUNTRY, Utils.getDefaultCountry());
            Tracks tracks = service.getArtistTopTrack(params[0], options);

            return mTrackBuilder.fromSpotifyTracks(tracks.tracks);
        }

        @Override
        protected void onPostExecute(ArrayList<MyTrack> topTracks) {
            mTopTracks = topTracks;
            mTopTracksAdapter.clear();
            mTopTracksAdapter.addAll(topTracks);

            mProgressDialog.dismiss();
            if (topTracks.size() < 1) {
                Toast.makeText(getActivity(), R.string.no_tracks_found, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(getActivity(),
                    null,
                    getResources().getString(R.string.loading_text),
                    true,
                    false);
        }

        @Override
        protected void onCancelled(ArrayList<MyTrack> topTracks) {
            mProgressDialog.dismiss();
        }
    }
}
