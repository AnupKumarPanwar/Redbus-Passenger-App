package com.gotobus.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gotobus.R;
import com.gotobus.classes.Seat;

import java.util.ArrayList;

public class SeatsAdapter extends RecyclerView.Adapter<SeatsAdapter.MyViewHolder> {
    Context context;
    ArrayList<Seat> seats;

    public SeatsAdapter(Context context, ArrayList<Seat> seats) {
        this.context = context;
        this.seats = seats;

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
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.seatNumber.setText(seats.get(position).id);
        if (seats.get(position).status.equals("Blank")){
            holder.container.setVisibility(View.GONE);
        }
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
        TextView seatNumber;
        LinearLayout container;

        public MyViewHolder(View itemView) {
            super(itemView);
            seatNumber = itemView.findViewById(R.id.seat_number);
            container = itemView.findViewById(R.id.container);
        }
    }
}
