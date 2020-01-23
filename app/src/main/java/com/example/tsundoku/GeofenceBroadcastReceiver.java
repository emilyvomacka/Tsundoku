package com.example.tsundoku;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private String TAG = "DEBUG";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("DEBUG", "YOU HIT THE ELLIOT BAY GEOFENCE");
//        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
//        if (geofencingEvent.hasError()) {
//            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
//            Log.d(TAG, errorMessage);
//            return;
//        }
//
//        // Get the transition type.
//        int geofenceTransition = geofencingEvent.getGeofenceTransition();
//
//        // Test that the reported transition was of interest.
//        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
//                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
//
//            // Get the geofences that were triggered. A single event can trigger
//            // multiple geofences.
//            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
//
//            // Get the transition details as a String.
//            String geofenceTransitionDetails = getGeofenceTransitionDetails(
//                    this,
//                    geofenceTransition,
//                    triggeringGeofences
//            );
//
//            // Send notification and log the transition details.
//            Log.d(TAG, geofenceTransitionDetails);
//        } else {
//            // Log the error.
//            Log.d(TAG, getString(R.string.geofence_transition_invalid_type,
//                    geofenceTransition));
//        }
//
   }
}
