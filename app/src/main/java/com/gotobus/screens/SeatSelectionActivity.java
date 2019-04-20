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
    SeatsAdapter leftSeatsAdapter, rightSeatsAdapter;
    ArrayList<Seat> seatsLeftRow;
    ArrayList<Seat> seatsRightRow;
    int counter = 1;
    int[] volvoLeftSeats = {41, 42, 37, 38, 33, 34, 29, 30, 25, 26, 21, 22, 17, 18, 13, 14, 9, 10, 5, 6, 1, 2};
    int[] volvoRightSeats = {43, 44, 39, 40, 35, 36, 31, 32, 27, 28, 23, 24, 19, 20, 15, 16, 11, 12, 7, 8, 3, 4};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);
        leftRow = findViewById(R.id.left_row);
        rightRow = findViewById(R.id.right_row);

        seatsLeftRow = new ArrayList<>();
        seatsRightRow = new ArrayList<>();


        for (int seatNumber : volvoLeftSeats) {
            seatsLeftRow.add(new Seat(String.valueOf(seatNumber), "Available"));
        }

        for (int seatNumber : volvoRightSeats) {
            seatsRightRow.add(new Seat(String.valueOf(seatNumber), "Available"));
        }

        leftSeatsAdapter = new SeatsAdapter(getApplicationContext(), seatsLeftRow);
        rightSeatsAdapter = new SeatsAdapter(getApplicationContext(), seatsRightRow);
        leftRow.setAdapter(leftSeatsAdapter);
        rightRow.setAdapter(rightSeatsAdapter);
    }
}
