package com.example.tsundoku;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private String TAG = "DEBUG";
    private static final String CHANNEL_ID = "TSUNDOKU";

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage =
                    GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            Log.d(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        int notificationId = 1;

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.book_stack)
                    .setContentTitle("YOU HAVE BOOKS AT HOME")
                    .setContentText("Might be a good time to check in with your unread book stack?")
                    .setPriority(NotificationCompat.PRIORITY_MAX);

            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(context);

            notificationManager.notify(notificationId, builder.build());
            notificationId += 1;
            Log.d(TAG, "geofence hit");
        }
    }
}
