package com.gotobus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.gotobus.R;
import com.gotobus.classes.Seat;

import java.util.ArrayList;

public class SeatsAdapter extends BaseAdapter {
    Context context;
    ArrayList<Seat> seats;

    public SeatsAdapter(Context context, ArrayList<Seat> seats) {
        this.context = context;
        this.seats = seats;

    }

    @Override
    public int getCount() {
        return seats.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.seat_item, parent, false);
//        TextView amount;
//        ImageView overlay;
//        overlay = view.findViewById(R.id.scratch_view);
//        if (seats.get(position).status.equals("SCRATCHED")) {
//            overlay.setVisibility(View.GONE);
//        }
//        amount = view.findViewById(R.id.amount);
//        amount.setText("You've won\n₹" + seats.get(position).amount);
        return view;
    }
}
