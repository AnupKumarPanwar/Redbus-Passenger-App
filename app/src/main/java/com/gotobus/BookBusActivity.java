package com.gotobus;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

public class BookBusActivity extends AppCompatActivity {

    TextView name, number, phone, arrivalTime, departureTime, pickupAddress, dropAddress, price;
    Button confirmBooking;
    String busId;
    int eta = 0;

    String source = "", destinataion = "";
    Calendar calendar;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_bus);

        name = findViewById(R.id.name);
        number = findViewById(R.id.bus_number);
        phone = findViewById(R.id.phone);
        arrivalTime = findViewById(R.id.arrival_time);
        departureTime = findViewById(R.id.departure_time);
        pickupAddress = findViewById(R.id.source);
        dropAddress = findViewById(R.id.destination);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:hh:mm aa");
        String currentDateandTime = sdf.format(new Date());

        Date date = null;


        if (getIntent().getExtras() != null) {
            Bundle data = getIntent().getExtras();
            name.setText(data.getString("name"));
            number.setText(data.getString("bus_number"));
            phone.setText(data.getString("phone"));
            source = data.getString("source");
            pickupAddress.setText(source);
            destinataion = data.getString("destination");
            dropAddress.setText(destinataion);
            eta = data.getInt("eta");


            try {
                date = sdf.parse(currentDateandTime);
                calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.SECOND, eta);

                departureTime.setText(calendar.getTime().toLocaleString());
//                Toast.makeText(getApplicationContext(), calendar.getTime().toString(), Toast.LENGTH_LONG).show();


            } catch (ParseException e) {
                e.printStackTrace();
//                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }


            busId = data.getString("bus_id");
        }

        setETA(source, destinataion, "");

    }

    private void setETA(String origin, String dest, String waypoints) {
        String url = getDirectionsUrl(origin, dest, waypoints);
        AndroidNetworking.get(url)
                .setOkHttpClient(NetworkCookies.okHttpClient)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray result = response.getJSONArray("routes");
                            if (result.length()>0) {
                                JSONObject route = result.getJSONObject(0);
                                JSONArray legs = route.getJSONArray("legs");

                                if (legs.length()>0) {
//                                    String eta = legs.getJSONObject(0).getJSONObject("duration").get("text").toString();
                                    int etaValue = Integer.parseInt(legs.getJSONObject(0).getJSONObject("duration").get("value").toString());
                                    calendar.add(Calendar.SECOND, etaValue);

                                    arrivalTime.setText(calendar.getTime().toLocaleString());
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Unable to calculate ETA", Toast.LENGTH_LONG).show();
                                }

                            }
                        }
                        catch (Exception e) {

                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });

    }

    private String getDirectionsUrl(String origin, String dest, String waypoints) {

        // Origin of route
        String str_origin = "origin=" + origin;

        // Destination of route
        String str_dest = "destination=" + dest;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        String apiKey = "key=" + getResources().getString(R.string.google_maps_key);
        String callback = "callback=initialize";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&" + apiKey + "&" + callback;

        parameters += "&waypoints=" + waypoints;
        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }
}

