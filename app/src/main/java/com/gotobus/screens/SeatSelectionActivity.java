package com.gotobus.screens;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;

import com.gotobus.R;
import com.gotobus.adapters.SeatsAdapter;
import com.gotobus.classes.Seat;

import java.util.ArrayList;

public class SeatSelectionActivity extends AppCompatActivity {
    GridView leftRow, rightRow;
    SeatsAdapter seatsAdapter;
    ArrayList<Seat> seats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);
        leftRow = findViewById(R.id.left_row);
        rightRow = findViewById(R.id.right_row);

        seats = new ArrayList<>();
        seats.add(new Seat("1", "Available"));
        seats.add(new Seat("1", "Available"));
        seats.add(new Seat("1", "Available"));
        seats.add(new Seat("1", "Available"));
        seats.add(new Seat("1", "Available"));
        seats.add(new Seat("1", "Available"));
        seatsAdapter = new SeatsAdapter(getApplicationContext(), seats);
        leftRow.setAdapter(seatsAdapter);
        rightRow.setAdapter(seatsAdapter);
    }
}
