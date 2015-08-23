/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import retrofit.RetrofitError;


/**
 * Fragment for diplaying artist's top tracks.
 */
public class TopTracksFragment extends Fragment {

    private static final String LOG_TAG = TopTracksFragment.class.getSimpleName();

    public static final String EXTRA_ARTIST = "artist";
    private static final String STATE_ARTIST = "state_artist";
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
            Bundle arguments = getArguments();
            if (arguments != null) {
                mArtist = arguments.getParcelable(TopTracksFragment.EXTRA_ARTIST);
                new SearchTrackTask(getActivity()).execute(mArtist.id);
            }
            mTopTracks = new ArrayList<>();
        }

        mTopTracksAdapter = new TopTracksAdapter(getActivity(), mTopTracks);

        if (mArtist != null) {
            // Set action bar subtitle
            ((ActionBarActivity) getActivity()).getSupportActionBar().setSubtitle(mArtist.name);
        }

        // Get a reference to the ListView, and attach the adapter
        ListView listView = (ListView) rootView.findViewById(R.id.list_view_top_tracks);
        listView.setAdapter(mTopTracksAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                intent.putExtra(PlayerFragment.EXTRA_TRACKS, mTopTracks);
                intent.putExtra(PlayerFragment.EXTRA_ARTIST, mArtist);
                intent.putExtra(PlayerFragment.EXTRA_CURRENT_TRACK_POSITION, i);
                startActivity(intent);
            }
        });

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
            try {
                // Query Spotify server
                SpotifyApi api = new SpotifyApi();
                SpotifyService service = api.getService();

                Map<String, Object> options = new HashMap<>();
                options.put(SpotifyService.COUNTRY, Utils.getDefaultCountry());
                Tracks tracks = service.getArtistTopTrack(params[0], options);

                return mTrackBuilder.fromSpotifyTracks(tracks.tracks);
            } catch (RetrofitError re) {
                Log.e(LOG_TAG, re.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<MyTrack> topTracks) {
            if (topTracks != null) {
                mTopTracks = topTracks;
                mTopTracksAdapter.clear();
                mTopTracksAdapter.addAll(topTracks);

                mProgressDialog.dismiss();
                if (topTracks.size() < 1) {
                    Toast.makeText(getActivity(), R.string.no_tracks_found, Toast.LENGTH_SHORT).show();
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
                        false);
            } else {
                Utils.showAlertDialog(getActivity(), R.string.no_network_error);
                cancel(true);
            }
        }

        @Override
        protected void onCancelled(ArrayList<MyTrack> topTracks) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }
}
