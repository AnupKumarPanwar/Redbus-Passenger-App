package com.gotobus.utility;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.gotobus.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class CustomMapUtils {

    private final Context context;

    public CustomMapUtils(Context context) {
        this.context = context;
    }

    public String getDirectionsUrl(LatLng origin, LatLng dest) {
        return getDirectionsUrl(origin, dest, "");
    }

    public static String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Download Exception", e.toString());
        } finally {
            Objects.requireNonNull(iStream).close();
            Objects.requireNonNull(urlConnection).disconnect();
        }
        return data;
    }

    public String getDirectionsUrl(LatLng origin, LatLng dest, String waypoints) {

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
