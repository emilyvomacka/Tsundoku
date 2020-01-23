package com.example.tsundoku;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("DEBUG", "OMG YOU HIT THE ELLIOT BAY BOOK GEOFENCE");
    }
}
