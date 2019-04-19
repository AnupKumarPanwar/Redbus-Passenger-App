package com.gotobus.screens;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.gotobus.utility.NetworkCookies;
import com.gotobus.R;
import com.gotobus.classes.Trip;
import com.gotobus.adapters.TripsAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TripsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TripsAdapter tripsAdapter;
    ArrayList<Trip> trips;
    String accessToken;
    SharedPreferences sharedPreferences;
    String PREFS_NAME = "MyApp_Settings";
    String baseUrl;
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        baseUrl = getResources().getString(R.string.base_url);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        accessToken = sharedPreferences.getString("access_token", null);
        recyclerView = findViewById(R.id.recycler_view);

        trips = new ArrayList<>();
        tripsAdapter = new TripsAdapter(getApplicationContext(), trips);
        recyclerView.setAdapter(tripsAdapter);

        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        AndroidNetworking.post(baseUrl + "/getTrips.php")
                .setOkHttpClient(NetworkCookies.okHttpClient)
                .addHeaders("Authorization", accessToken)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject result = response.getJSONObject("result");
                            boolean success = Boolean.parseBoolean(result.get("success").toString());
                            if (success) {
                                JSONArray data = result.getJSONArray("data");
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject trip = data.getJSONObject(i);
                                    String date = trip.get("booked_at").toString();
                                    String fare = trip.get("fare").toString();
                                    String bus = trip.get("name").toString();
                                    String type = trip.get("bus_type").toString();
                                    String status = trip.get("status").toString();
                                    String[] source = trip.get("pickup_point").toString().split(",");
                                    String[] destination = trip.get("dropoff_point").toString().split(",");

                                    List<Address> addresses = geocoder.getFromLocation(Double.parseDouble(source[0]), Double.parseDouble(source[1]), 1);
                                    Address address = addresses.get(0);
                                    String pickupAddress = address.getAddressLine(0);

                                    addresses = geocoder.getFromLocation(Double.parseDouble(destination[0]), Double.parseDouble(destination[1]), 1);
                                    address = addresses.get(0);
                                    String dropoffAddress = address.getAddressLine(0);

                                    trips.add(new Trip(date, bus, fare, type, status, pickupAddress, dropoffAddress));
                                }

                                Log.d("trips", "onResponse: " + trips.get(0).bus);

                                tripsAdapter.notifyDataSetChanged();
                            } else {
                                String message = result.get("message").toString();
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
//                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Toast.makeText(getApplicationContext(), error.getErrorBody(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
