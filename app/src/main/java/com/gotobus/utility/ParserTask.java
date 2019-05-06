package com.gotobus.utility;

import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.gotobus.screens.SelectedBusActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

    final String lineColor;
    public PolylineOptions lineOptions;
    String activity;

    ParserTask(String lineColor, String activity) {
        this.lineColor = lineColor;
        this.activity = activity;
        lineOptions = null;

    }

    // Parsing the data in non-ui thread
    @Override
    public List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = new JSONObject(jsonData[0]);
            DirectionsJSONParser parser = new DirectionsJSONParser();

            routes = parser.parse(jObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return routes;
    }

    @Override
    public void onPostExecute(List<List<HashMap<String, String>>> result) {
        if (result == null) {
            return;
        }
        ArrayList points;
        MarkerOptions markerOptions = new MarkerOptions();

        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList();
            lineOptions = new PolylineOptions();

            List<HashMap<String, String>> path = result.get(i);

            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                double lng = Double.parseDouble(Objects.requireNonNull(point.get("lng")));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            lineOptions.addAll(points);
            lineOptions.width(18);
            lineOptions.color(Color.parseColor(lineColor));
            lineOptions.geodesic(true);

            if (activity.equals("SelectedBusActivity")) {
                SelectedBusActivity.mMap.addPolyline(lineOptions);
            }

        }
    }

//    public void addPolylinesOnMap(GoogleMap mMap) {
//        try {
//            mMap.addPolyline(lineOptions);
//        } catch (Exception e) {
//            Log.d("Polyline", e.getMessage());
//        }
//    }
}