/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
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
import com.example.android.spotifystreamer.service.PlayerService;
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
    private static final String SELECTED_KEY = "selected_position";

    private Artist mArtist;
    private ArrayList<MyTrack> mTopTracks;
    private TopTracksAdapter mTopTracksAdapter;
    private ListView mTracksListView;
    private int mPosition = ListView.INVALID_POSITION;


    public interface TrackSelectedCallback {
        /** Callback for when an item has been selected. */
        void onTrackSelected(Artist artist, ArrayList<MyTrack> tracks, int position);
    }

    private BroadcastReceiver mOnMusicPlayerSongChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int position = intent.getIntExtra(PlayerService.SONG_POSITION_EXTRA, 0);
            Artist artist = intent.getParcelableExtra(PlayerService.ARTIST_EXTRA);
            if (mArtist == artist) {
                mTracksListView.setItemChecked(position, true); //make sure selected item is in view
            }
            mPosition = position;
        }
    };

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
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(mArtist.name);
        }

        // Get a reference to the ListView, and attach the adapter
        mTracksListView = (ListView) rootView.findViewById(R.id.list_view_top_tracks);
        mTracksListView.setAdapter(mTopTracksAdapter);
        mTracksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                ((TrackSelectedCallback) getActivity())
                        .onTrackSelected(mArtist, mTopTracks, position);
                mPosition = position;
            }
        });

        // Restore scroll position
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
            mTracksListView.setSelection(mPosition);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_ARTIST, mArtist);
        outState.putParcelableArrayList(STATE_TRACKS, mTopTracks);
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
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

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mOnMusicPlayerSongChanged,
                new IntentFilter(PlayerService.MUSIC_PLAYER_SONG_CHANGED_NOTIFICATION));
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mOnMusicPlayerSongChanged);
        super.onStop();
    }

}
