package com.gotobus.screens;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.gotobus.R;
import com.gotobus.adapters.SeatsAdapter;
import com.gotobus.classes.Seat;

import java.util.ArrayList;

public class SeatSelectionActivity extends AppCompatActivity {
    RecyclerView seatsGrid;
    SeatsAdapter leftSeatsAdapter, rightSeatsAdapter;
    ArrayList<Seat> seatsLeftRow;
    ArrayList<Seat> seatsRightRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);
        seatsGrid = findViewById(R.id.seats);

        seatsGrid.setLayoutManager(new GridLayoutManager(getApplicationContext(), 5));

        seatsLeftRow = new ArrayList<>();
        seatsRightRow = new ArrayList<>();

        int seatNumber = 41;
        while (seatNumber>0) {
            for (int i=0; i<4; i++) {
                if (i==2) {
                    seatsLeftRow.add(new Seat(null, "Blank"));
                }
                seatsLeftRow.add(new Seat(String.valueOf(seatNumber+i), "Available"));
            }
            seatNumber-=4;
        }


        leftSeatsAdapter = new SeatsAdapter(getApplicationContext(), seatsLeftRow);
        rightSeatsAdapter = new SeatsAdapter(getApplicationContext(), seatsRightRow);
        seatsGrid.setAdapter(leftSeatsAdapter);
    }
}
