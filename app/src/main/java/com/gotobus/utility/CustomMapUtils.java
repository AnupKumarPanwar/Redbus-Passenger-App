package com.gotobus.utility;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.gotobus.R;

public class CustomMapUtils {

    private final Context context;

    public CustomMapUtils(Context context) {
        this.context = context;
    }

    public String getDirectionsUrl(LatLng origin, LatLng dest) {
        return getDirectionsUrl(origin, dest, "");
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest, String waypoints) {

        Log.d("waypoints_url", "getDirectionsUrl: " + waypoints);

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        String apiKey = "key=" + context.getResources().getString(R.string.google_maps_key);
        String callback = "callback=initialize";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&" + apiKey + "&" + callback;

        Log.d("waypoints_url", "getDirectionsUrl: " + waypoints);

        parameters += "&waypoints=" + waypoints;
        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        url = url.replaceAll(" ", "%20");

        return url;
    }
}
