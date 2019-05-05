package com.gotobus.screens;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gotobus.R;
import com.gotobus.utility.Journey;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

import es.dmoral.toasty.Toasty;

import static com.gotobus.utility.Journey.dropoffAddress;
import static com.gotobus.utility.Journey.pickupAddress;

public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {

    int fare;
    TextView informationTV, pickupAddressTV, dropoffAddressTV, fareTV, busNameTV, journeyDateTV, seatsBookedTV;
    LinearLayout container, containerError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        container = findViewById(R.id.container);
        containerError = findViewById(R.id.container_error);

        informationTV = findViewById(R.id.information);
        pickupAddressTV = findViewById(R.id.pickup_point);
        dropoffAddressTV = findViewById(R.id.dropoff_point);
        fareTV = findViewById(R.id.fare);
        busNameTV = findViewById(R.id.bus_name);
        journeyDateTV = findViewById(R.id.journey_date);
        seatsBookedTV = findViewById(R.id.seats_booked);

        Bundle bundle = getIntent().getExtras();


        fare = Journey.fare;
        pickupAddressTV.setText(pickupAddress);
        dropoffAddressTV.setText(dropoffAddress);
        fareTV.setText("Rs. " + Journey.fare);
        busNameTV.setText(Journey.busName);
        journeyDateTV.setText(Journey.journeyDate);
        seatsBookedTV.setText(Journey.getSeats());
        startPayment();

    }

    @Override
    public void onPaymentSuccess(String s) {
        Toasty.success(getApplicationContext(), "Booking confirmed", Toasty.LENGTH_LONG).show();

        informationTV.setText("Txn ID: " + s);
        containerError.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);

        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        Bitmap bitmap = getScreenShot(rootView);
        Uri filePath = store(bitmap, s + ".png");
    }


    private void shareImage(Uri file) {
        Uri uri = file;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "GoTo");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "Download GoTo - The realtime bus booking app.");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            startActivity(Intent.createChooser(intent, "Share ticket"));
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "No App Available", Toast.LENGTH_SHORT).show();
        }
    }

    public Bitmap getScreenShot(View view) {
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public Uri store(Bitmap bm, String fileName) {
        Uri bmpUri = null;

        try {
            // This way, you don't need to request external read/write permission.
            File file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();

        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(), e.toString(), 5000).show();
//            informationTV.setText(e.toString());
        }
        return bmpUri;
    }

    @Override
    public void onPaymentError(int i, String s) {
        container.setVisibility(View.GONE);
        containerError.setVisibility(View.VISIBLE);
        Toasty.error(getApplicationContext(), "Payment failed", Toasty.LENGTH_LONG).show();
    }

    public void startPayment() {
        /**
         * Instantiate Checkout
         */
        Checkout checkout = new Checkout();

        /**
         * Set your logo here
         */
        checkout.setImage(R.drawable.goto_trans);

        /**
         * Reference to current activity
         */
        final Activity activity = this;

        /**
         * Pass your payment options to the Razorpay Checkout as a JSONObject
         */
        try {
            JSONObject options = new JSONObject();

            /**
             * Merchant Name
             * eg: ACME Corp || HasGeek etc.
             */
            options.put("name", "Goto Bus Pvt Ltd.");

            /**
             * Description can be anything
             * eg: Order #123123
             *     Invoice Payment
             *     etc.
             */
            options.put("description", "Happy Journey");

            options.put("currency", "INR");

            /**
             * Amount is always passed in PAISE
             * Eg: "500" = Rs 5.00
             */
            options.put("amount", fare * 100);

            checkout.open(activity, options);
        } catch (Exception e) {
//            Log.e(TAG, "Error in starting Razorpay Checkout", e);
            Toasty.error(getApplicationContext(), e.toString(), Toasty.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
