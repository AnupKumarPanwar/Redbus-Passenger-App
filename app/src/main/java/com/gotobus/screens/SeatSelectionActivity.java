package com.gotobus.screens;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.gotobus.R;
import com.gotobus.adapters.SeatsAdapter;
import com.gotobus.classes.Seat;

import java.util.ArrayList;
import java.util.List;

public class SeatSelectionActivity extends AppCompatActivity {
    RecyclerView seatsGrid;
    SeatsAdapter leftSeatsAdapter, rightSeatsAdapter;
    ArrayList<Seat> seatsLeftRow;
    ArrayList<Seat> seatsRightRow;
    String busName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);
        seatsGrid = findViewById(R.id.seats);

        seatsGrid.setLayoutManager(new GridLayoutManager(getApplicationContext(), 5));

        busName = getIntent().getExtras().get("bus_name").toString();
        busName = busName.toLowerCase();
//        Toast.makeText(getApplicationContext(), busName, Toast.LENGTH_LONG).show();

        ArrayList<Integer> occupied = new ArrayList<>();
        for (int i = 0; i < busName.length(); i++) {
            int seatNum = (busName.charAt(i) - 97) % 44;
//            Toast.makeText(getApplicationContext(), String.valueOf(seatNum), Toast.LENGTH_LONG).show();
            if (occupied.indexOf(seatNum) == -1)
                occupied.add(seatNum);
        }

        busName = busName.toUpperCase();
        for (int i = 0; i < busName.length(); i++) {
            int seatNum = (busName.charAt(i) - 65) % 44;
//            Toast.makeText(getApplicationContext(), String.valueOf(seatNum), Toast.LENGTH_LONG).show();
            if (occupied.indexOf(seatNum) == -1)
                occupied.add(seatNum);
        }

        List<Integer> occupied2;
        int counter = 1;
        if (occupied.size() > 25) {
            occupied2 = occupied.subList(0, 24);
        } else {
            while (occupied.size() < 25) {
                for (int i = 0; i < 10; i++) {
                    int seatNum = (busName.charAt(i) + counter) % 44;
//            Toast.makeText(getApplicationContext(), String.valueOf(seatNum), Toast.LENGTH_LONG).show();

                    occupied.add(seatNum);
                    counter++;
                }
            }
            occupied2 = occupied;
        }

        seatsLeftRow = new ArrayList<>();
        seatsRightRow = new ArrayList<>();
        leftSeatsAdapter = new SeatsAdapter(getApplicationContext(), seatsLeftRow);
        seatsGrid.setAdapter(leftSeatsAdapter);


        int seatNumber = 41;
        while (seatNumber > 0) {
            for (int i = 0; i < 4; i++) {
                if (i == 2) {
                    seatsLeftRow.add(new Seat(null, "Blank"));
                }
                if (occupied2.indexOf(seatNumber + i) != -1) {
                    seatsLeftRow.add(new Seat(String.valueOf(seatNumber + i), "Booked"));
                } else
                    seatsLeftRow.add(new Seat(String.valueOf(seatNumber + i), "Available"));
                leftSeatsAdapter.notifyDataSetChanged();
            }
            seatNumber -= 4;
        }


        rightSeatsAdapter = new SeatsAdapter(getApplicationContext(), seatsRightRow);
    }
}
