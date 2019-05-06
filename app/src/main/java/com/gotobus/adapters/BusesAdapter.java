package com.gotobus.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gotobus.R;
import com.gotobus.classes.Bus;
import com.gotobus.screens.SelectedBusActivity;
import com.gotobus.utility.Journey;

import java.util.ArrayList;

public class BusesAdapter extends RecyclerView.Adapter<BusesAdapter.MyViewHolder> {
    private final Context context;
    private final ArrayList<Bus> buses;

    public BusesAdapter(Context context, ArrayList<Bus> buses) {
        this.context = context;
        this.buses = buses;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        View view = LayoutInflater.from(context).inflate(R.layout.bus_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, final int i) {
        myViewHolder.fare.setText("₹" + buses.get(i).fare);
        myViewHolder.type.setText(buses.get(i).type);
        myViewHolder.name.setText(buses.get(i).name);
        myViewHolder.departureTime.setText(buses.get(i).departureTime);
        myViewHolder.arrivalTime.setText(buses.get(i).arrivalTime);
        myViewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SelectedBusActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Journey.busName = buses.get(i).name;
                intent.putExtra("route_id", buses.get(i).routeId);
                intent.putExtra("bus_name", buses.get(i).name);
                intent.putExtra("bus_type", buses.get(i).type);
                intent.putExtra("fare", buses.get(i).fare);
                intent.putExtra("arrival_time", buses.get(i).arrivalTime);
                intent.putExtra("departure_time", buses.get(i).departureTime);
                intent.putExtra("sourceLat", buses.get(i).departureTime);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return buses.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView type;
        final TextView fare;
        final TextView departureTime;
        final TextView arrivalTime;
        final LinearLayout container;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.bus_name);
            type = itemView.findViewById(R.id.type);
            fare = itemView.findViewById(R.id.fare);
            departureTime = itemView.findViewById(R.id.departure_time);
            arrivalTime = itemView.findViewById(R.id.arrival_time);
            container = itemView.findViewById(R.id.container);
        }
    }
}
