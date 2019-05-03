package com.gotobus.screens;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.gotobus.R;
import com.gotobus.adapters.BusesAdapter;
import com.gotobus.classes.Bus;
import com.gotobus.utility.Journey;
import com.gotobus.utility.NetworkCookies;
import com.gotobus.utility.ResponseValidator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ViewMoreActivity extends AppCompatActivity {

    TextView tripDate;
    int mYear, mMonth, mDay, mHour, mMinute;

    RecyclerView recyclerView;
    BusesAdapter busesAdapter;
    ArrayList<Bus> buses;
    String accessToken;
    SharedPreferences sharedPreferences;
    String PREFS_NAME = "MyApp_Settings";
    String baseUrl;

    String sourceLat, sourceLng, destinationLat, destinationLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_more);

        baseUrl = getResources().getString(R.string.base_url);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        accessToken = sharedPreferences.getString("access_token", null);

        sourceLat = Journey.sourceLat;
        sourceLng = Journey.sourceLng;
        destinationLat = Journey.destinationLat;
        destinationLng = Journey.destinationLng;

//        Toast.makeText(getApplicationContext(), destinationLng.toString(), Toast.LENGTH_LONG).show();
        searchBuses();

        tripDate = findViewById(R.id.trip_date);
        tripDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(ViewMoreActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String sDayOfMonth = dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                        String sMonth = month < 10 ? "0" + month : String.valueOf(month);
                        tripDate.setText(sDayOfMonth + "/" + sMonth + "/" + year + " \uD83D\uDD3B");
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                c.add(Calendar.MONTH, 2);
                datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
                datePickerDialog.show();
            }
        });

        buses = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
//        buses.add(new Bus("Dhall Tours and Travels ", "450", "AC", "Now", "Tomorrow 5:40"));
//        buses.add(new Bus("Vishnu Buses", "450", "AC", "Now", "Tomorrow 5:40"));
//        buses.add(new Bus("Dhall Travels", "450", "AC", "Now", "Tomorrow 5:40"));
//        buses.add(new Bus("Dhall Travels", "450", "AC", "Now", "Tomorrow 5:40"));
        busesAdapter = new BusesAdapter(getApplicationContext(), buses);
        recyclerView.setAdapter(busesAdapter);
    }

    private void searchBuses() {
        AndroidNetworking.post(baseUrl + "/searchLater.php")
                .setOkHttpClient(NetworkCookies.okHttpClient)
                .addHeaders("Authorization", accessToken)
                .addBodyParameter("sourceLat", sourceLat)
                .addBodyParameter("sourceLng", sourceLng)
                .addBodyParameter("destinationLat", destinationLat)
                .addBodyParameter("destinationLng", destinationLng)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (ResponseValidator.validate(ViewMoreActivity.this, response)) {
                                JSONObject result = response.getJSONObject("result");
                                boolean success = Boolean.parseBoolean(result.get("success").toString());
                                if (success) {
                                    JSONArray data = result.getJSONArray("data");
                                    for (int i = 0; i < data.length(); i++) {
                                        JSONObject bus = data.getJSONObject(i);
                                        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                        final Date dateObj = sdf.parse(bus.get("departure_time").toString());
                                        String sdf2 = new SimpleDateFormat("hh:mm a").format(dateObj);

                                        buses.add(new Bus(bus.get("name").toString(), "500", bus.get("bus_type").toString(), sdf2, "09:00PM"));
                                    }
                                    busesAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
