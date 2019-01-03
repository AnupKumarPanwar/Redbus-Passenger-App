package com.gotobus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class BookBusActivity extends AppCompatActivity {

    TextView name, number, phone, arrivalTime, departureTime, pickupAddress, dropAddress, price;
    Button confirmBooking;
    String busId;

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

        if (getIntent().getExtras() != null) {
            Bundle data = getIntent().getExtras();
            name.setText(data.getString("name"));
            number.setText(data.getString("bus_number"));
            phone.setText(data.getString("phone"));
            pickupAddress.setText(data.getString("source"));
            dropAddress.setText(data.getString("destination"));

            busId = data.getString("bus_id");
        }

    }
}
