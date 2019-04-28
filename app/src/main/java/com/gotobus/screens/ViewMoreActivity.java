package com.gotobus.screens;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.gotobus.R;
import com.gotobus.adapters.BusesAdapter;
import com.gotobus.classes.Bus;

import java.util.ArrayList;
import java.util.Calendar;

public class ViewMoreActivity extends AppCompatActivity {

    TextView tripDate;
    int mYear, mMonth, mDay, mHour, mMinute;

    RecyclerView recyclerView;
    BusesAdapter busesAdapter;
    ArrayList<Bus> buses;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_more);
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
        buses.add(new Bus("Dhall Tours and Travels ", "450", "AC", "Now", "Tomorrow 5:40"));
        buses.add(new Bus("Vishnu Buses", "450", "AC", "Now", "Tomorrow 5:40"));
        buses.add(new Bus("Dhall Travels", "450", "AC", "Now", "Tomorrow 5:40"));
        buses.add(new Bus("Dhall Travels", "450", "AC", "Now", "Tomorrow 5:40"));
        busesAdapter = new BusesAdapter(getApplicationContext(), buses);
        recyclerView.setAdapter(busesAdapter);
    }
}
