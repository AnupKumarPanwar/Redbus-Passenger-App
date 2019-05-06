package com.gotobus.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gotobus.R;
import com.gotobus.adapters.SeatsAdapter;
import com.gotobus.classes.Seat;
import com.gotobus.interfaces.SeatSelectListener;
import com.gotobus.utility.Journey;
import com.razorpay.Checkout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class SeatSelectionActivity extends AppCompatActivity {
    private RecyclerView seatsGrid;
    private SeatsAdapter seatsAdapter;
    private ArrayList<Seat> seatsRow;
    private String busName;
    private int numSeatsSelected = 0;
    private TextView fareTextView;
    private Button confirmBookingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);
        seatsGrid = findViewById(R.id.seats);

        fareTextView = findViewById(R.id.fare);
        confirmBookingBtn = findViewById(R.id.book_confirm);
        confirmBookingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numSeatsSelected <= 0) {
                    Toasty.error(getApplicationContext(), "Please select atleast 1 seat", Toasty.LENGTH_LONG).show();
                } else {
                    int fare = numSeatsSelected * 500;
                    Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                    intent.putExtra("fare", fare);
                    Journey.fare = fare;
                    startActivity(intent);
                }
            }
        });

        seatsGrid.setLayoutManager(new GridLayoutManager(getApplicationContext(), 5));

        busName = Objects.requireNonNull(getIntent().getExtras().get("bus_name")).toString();
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

        seatsRow = new ArrayList<>();
        seatsAdapter = new SeatsAdapter(getApplicationContext(), seatsRow);
        seatsGrid.setAdapter(seatsAdapter);


        int seatNumber = 41;
        while (seatNumber > 0) {
            for (int i = 0; i < 4; i++) {
                if (i == 2) {
                    seatsRow.add(new Seat(null, "Blank"));
                }
                if (occupied2.indexOf(seatNumber + i) != -1) {
                    seatsRow.add(new Seat(String.valueOf(seatNumber + i), "Booked"));
                } else
                    seatsRow.add(new Seat(String.valueOf(seatNumber + i), "Available"));
                seatsAdapter.notifyDataSetChanged();
            }
            seatNumber -= 4;
        }

        seatsAdapter.setSeatSelectListener(new SeatSelectListener() {
            @Override
            public void onChange(int n) {
                numSeatsSelected = n;
                int fare = numSeatsSelected * 500;
                fareTextView.setText("Rs. " + fare);
            }
        });

        Checkout.preload(getApplicationContext());

    }

}
