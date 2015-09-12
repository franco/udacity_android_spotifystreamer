/*
 * Copyright (c) 2015 Franco Sebregondi.
 */

package com.example.android.spotifystreamer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaDescription;
import android.os.RemoteException;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.android.spotifystreamer.service.PlayerService;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Manages the notification.
 *
 * Concept copied from sample UniversalMusicPlayer
 * https://github.com/googlesamples/android-UniversalMusicPlayer
 */
public class MediaNotificationManager extends BroadcastReceiver {

    private static final String LOG_TAG = MediaNotificationManager.class.getSimpleName();
    private static final int NOTIFICATION_ID = 0;
    private static final int REQUEST_CODE = 100;

    public static final String ACTION_PAUSE = "com.example.android.spotifystreamer.ACTION_PAUSE";
    public static final String ACTION_PLAY = "com.example.android.spotifystreamer.ACTION_PLAY";
    public static final String ACTION_PREV = "com.example.android.spotifystreamer.ACTION_PREV";
    public static final String ACTION_NEXT = "com.example.android.spotifystreamer.ACTION_NEXT";

    private PlayerService mService;
    private MediaSessionCompat.Token mSessionToken;
    private MediaControllerCompat mMediaController;

    private final NotificationManager mNotificationManager;
    private final PendingIntent mPauseIntent;
    private final PendingIntent mPlayIntent;
    private final PendingIntent mPreviousIntent;
    private final PendingIntent mNextIntent;

    private MediaMetadataCompat mMetadata;
    private PlaybackStateCompat mPlaybackState;

    private boolean mStarted = false;


    public MediaNotificationManager(PlayerService service) {
        mService = service;
        updateSessionToken();
        mNotificationManager =
                (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);

        // Already create the pending intents here for performance reasons
        String pkg = mService.getPackageName();
        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before {@link #stopNotification} is called.
     */
    public void startNotification() {
        if (!mStarted) {
            mMetadata = mMediaController.getMetadata();
            mPlaybackState = mMediaController.getPlaybackState();

            // The notification must be updated after setting started to true
            Notification notification = createNotification();
            if (notification != null) {
                mMediaController.registerCallback(mMediaControllerCallback);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_PREV);
                mService.registerReceiver(this, filter);

                mService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
            }

            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    /**
     * Removes the notification and stops tracking the session. If the session
     * was destroyed this has no effect.
     */
    public void stopNotification() {
        if (mStarted) {
            mStarted = false;
            mMediaController.unregisterCallback(mMediaControllerCallback);
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            mService.stopForeground(true);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(LOG_TAG, "Received intent with action " + action);
        switch (action) {
            case ACTION_PAUSE:
                mMediaController.getTransportControls().pause();
                break;
            case ACTION_PLAY:
                mMediaController.getTransportControls().play();
                break;
            case ACTION_NEXT:
                mMediaController.getTransportControls().skipToNext();
                break;
            case ACTION_PREV:
                mMediaController.getTransportControls().skipToPrevious();
                break;
            default:
                Log.w(LOG_TAG, "Unknown intent ignored. Action=" + action);
        }
    }

    /**
     * Update the state based on a change on the session token. Called either when
     * we are running for the first time or when the media session owner has destroyed the session
     */
    private void updateSessionToken() {
        MediaSessionCompat.Token freshToken = mService.getSessionToken();

        if (mSessionToken == null || !mSessionToken.equals(freshToken)) {
            if (mMediaController != null) {
                mMediaController.unregisterCallback(mMediaControllerCallback);
            }
            mSessionToken = freshToken;
            try {
                mMediaController = new MediaControllerCompat(mService, mSessionToken);
                if (mStarted) {
                    mMediaController.registerCallback(mMediaControllerCallback);
                }
            } catch (RemoteException e) {
                mMediaController = null;
            }
        }
    }

    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    mPlaybackState = state;
                    Log.d(LOG_TAG, "Received new playback state " + state);
                    if (state.getState() == PlaybackStateCompat.STATE_STOPPED
                            || state.getState() == PlaybackStateCompat.STATE_NONE) {
                        stopNotification();
                    } else {
                        createAndNotifyNotification();
                    }
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    mMetadata = metadata;
                    Log.d(LOG_TAG, "Received new metadata " + metadata);
                    createAndNotifyNotification();
                }

                @Override
                public void onSessionDestroyed() {
                    Log.d(LOG_TAG, "Session was destroyed, resetting to new session token");
                    updateSessionToken();
                }
            };

    private Notification createNotification() {
        if (mMetadata == null || mPlaybackState == null) {
            return null;
        }

        MediaDescriptionCompat description = mMetadata.getDescription();

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mService);

        Target artworkTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                notificationBuilder.setLargeIcon(bitmap);
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {}

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };

        Picasso.with(mService).load(mMetadata.getDescription().getIconUri()).into(artworkTarget);


        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle()
                .setMediaSession(mSessionToken)
                .setShowActionsInCompactView(new int[]{1}); // show only play/pause in compact view

        notificationBuilder
                .setStyle(style)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setUsesChronometer(true);


        notificationBuilder.addAction(R.drawable.ic_skip_previous_white_24dp,
                mService.getString(R.string.previews_button_label), mPreviousIntent);
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PAUSED) {
            notificationBuilder.addAction(R.drawable.ic_play_arrow_white_24dp,
                    mService.getString(R.string.play_button_label), mPlayIntent);
        } else {
            notificationBuilder.addAction(R.drawable.ic_pause_white_24dp,
                    mService.getString(R.string.pause_button_label), mPauseIntent);
        }
        notificationBuilder.addAction(R.drawable.ic_skip_next_white_24dp,
                mService.getString(R.string.next_button_label), mNextIntent);


        notificationBuilder.setContentIntent(createContentIntent());

        setNotificationPlaybackState(notificationBuilder);

        return notificationBuilder.build();
    }

    private void createAndNotifyNotification() {
        Notification notification = createNotification();
        if (notification != null) {
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        Log.d(LOG_TAG, "updateNotificationPlaybackState. mPlaybackState=" + mPlaybackState);
        if (mPlaybackState == null || !mStarted) {
            Log.d(LOG_TAG, "updateNotificationPlaybackState. cancelling notification!");
            mService.stopForeground(true);
            return;
        }
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING
                && mPlaybackState.getPosition() >= 0) {
            Log.d(LOG_TAG, "updateNotificationPlaybackState. updating playback position to " +
                    ((System.currentTimeMillis() - mPlaybackState.getPosition()) / 1000) + " seconds");
            builder
                    .setWhen(System.currentTimeMillis() - mPlaybackState.getPosition())
                    .setShowWhen(true)
                    .setUsesChronometer(true);
        } else {
            Log.d(LOG_TAG, "updateNotificationPlaybackState. hiding playback position");
            builder
                    .setWhen(0)
                    .setShowWhen(false)
                    .setUsesChronometer(false);
        }

        // Make sure that the notification can be dismissed by the user when we are not playing:
        builder.setOngoing(mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING);
    }

    private PendingIntent createContentIntent() {
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(mService, FullScreenPlayerActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(mService, REQUEST_CODE, resultIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
