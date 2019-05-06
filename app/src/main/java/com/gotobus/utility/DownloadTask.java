package com.gotobus.utility;

import android.os.AsyncTask;
import android.util.Log;

import static com.gotobus.utility.CustomMapUtils.downloadUrl;

public class DownloadTask extends AsyncTask<String, Void, String> {
    final String lineColor;
    ParserTask parserTask;
    String activity;

    public DownloadTask(String lineColor, String activity) {
        this.lineColor = lineColor;
        this.activity = activity;
    }

    @Override
    public String doInBackground(String... url) {

        String data = "";

        try {
            Log.d("download_url", url[0]);
            data = downloadUrl(url[0]);
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return data;
    }

    @Override
    public void onPostExecute(String result) {
        super.onPostExecute(result);

        ParserTask parserTask = new ParserTask(lineColor, activity);
        parserTask.execute(result);
    }

//    public void addPolylinesOnMap(GoogleMap mMap) {
//        try {
//            parserTask.addPolylinesOnMap(mMap);
//        } catch (Exception e) {
//            Log.d("Polyline", e.getMessage());
//        }
//    }
}