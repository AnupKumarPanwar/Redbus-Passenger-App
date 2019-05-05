package com.gotobus.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gotobus.R;
import com.gotobus.classes.Seat;
import com.gotobus.interfaces.SeatSelectListener;
import com.gotobus.utility.Journey;

import java.util.ArrayList;

public class SeatsAdapter extends RecyclerView.Adapter<SeatsAdapter.MyViewHolder> {
    private final Context context;
    private final ArrayList<Seat> seats;
    private final ArrayList<String> selectedSeats;
    private int numSeatsSelected;

    private SeatSelectListener seatSelectListener;


    public SeatsAdapter(Context context, ArrayList<Seat> seats) {
        this.context = context;
        this.seats = seats;
        this.numSeatsSelected = 0;
        this.selectedSeats = new ArrayList<>();

    }

    public void setSeatSelectListener(SeatSelectListener seatSelectListener) {
        this.seatSelectListener = seatSelectListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.seat_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.seatNumber.setText(seats.get(position).id);
        if (seats.get(position).status.equals("Booked")) {
            holder.container.setBackgroundColor(Color.GRAY);
        }
        if (seats.get(position).status.equals("Blank")) {
            holder.container.setVisibility(View.GONE);
        }
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (seats.get(position).status.equals("Selected")) {
                    holder.container.setBackgroundColor(Color.TRANSPARENT);
                    seats.get(position).setStatus("Available");
                    --numSeatsSelected;
                    selectedSeats.remove(seats.get(position).id);
                    if (seatSelectListener != null) {
                        seatSelectListener.onChange(numSeatsSelected);
                    }


                } else if (seats.get(position).status.equals("Available")) {
                    holder.container.setBackgroundColor(Color.parseColor("#22ee33"));
                    seats.get(position).setStatus("Selected");
                    ++numSeatsSelected;
                    selectedSeats.add(seats.get(position).id);
                    if (seatSelectListener != null) {
                        seatSelectListener.onChange(numSeatsSelected);
                    }

                }

                Journey.seats = selectedSeats;
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return seats.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView seatNumber;
        final RelativeLayout container;

        MyViewHolder(View itemView) {
            super(itemView);
            seatNumber = itemView.findViewById(R.id.seat_number);
            container = itemView.findViewById(R.id.container);
        }
    }
}
