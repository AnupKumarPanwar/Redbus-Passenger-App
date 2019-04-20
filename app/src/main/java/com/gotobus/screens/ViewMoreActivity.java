package com.gotobus.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import com.gotobus.R;

import java.util.Calendar;
import java.util.Date;

public class ViewMoreActivity extends AppCompatActivity {

    DatePicker datePicker;
    Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_more);
        datePicker = findViewById(R.id.date_picker);
        datePicker.setMinDate(System.currentTimeMillis() - 1000);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, 2);
        datePicker.setMaxDate(cal.getTimeInMillis());

        searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SeatSelectionActivity.class);
                startActivity(intent);
            }
        });
    }
}
