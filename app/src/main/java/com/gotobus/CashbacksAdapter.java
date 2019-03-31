package com.gotobus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class CashbacksAdapter extends BaseAdapter {
    Context context;
    ArrayList<Cashback> cashbacks;

    public CashbacksAdapter(Context context, ArrayList<Cashback> cashbacks) {
        this.context = context;
        this.cashbacks = cashbacks;

    }

    @Override
    public int getCount() {
        return cashbacks.size();
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
        View view = LayoutInflater.from(context).inflate(R.layout.cashback_item, parent, false);
        TextView amount;
        ImageView overlay;
        overlay = view.findViewById(R.id.scratch_view);
        if (cashbacks.get(position).status.equals("SCRATCHED")) {
            overlay.setVisibility(View.GONE);
        }
        amount = view.findViewById(R.id.amount);
        amount.setText("You've won\n₹" + cashbacks.get(position).amount);
        return view;
    }
}
