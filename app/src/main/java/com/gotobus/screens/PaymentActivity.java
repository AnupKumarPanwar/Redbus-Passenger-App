package com.gotobus.screens;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
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

    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    boolean mStoragePermissionGranted = false;
    ImageView downloadTicket;
    Handler handler;
    Runnable runnable;
    private int fare;
    private TextView informationTV;
    private TextView pickupAddressTV;
    private TextView dropoffAddressTV;
    private TextView fareTV;
    private TextView busNameTV;
    private TextView journeyDateTV;
    private TextView seatsBookedTV;
    private LinearLayout container;
    private LinearLayout containerError;
    private String fileName;

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
        downloadTicket = findViewById(R.id.download_ticket);
        downloadTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getStoragePermission();
                takeScreenshot();

            }
        });

        Bundle bundle = getIntent().getExtras();


        fare = Journey.fare;
        pickupAddressTV.setText(pickupAddress);
        dropoffAddressTV.setText(dropoffAddress);
        fareTV.setText("Rs. " + Journey.fare);
        busNameTV.setText(Journey.busName);
        journeyDateTV.setText(Journey.journeyDate);
        seatsBookedTV.setText(Journey.getSeats());
        startPayment();

        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                getStoragePermission();
                takeScreenshot();
            }
        };

    }

    private void getStoragePermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            mStoragePermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void takeScreenshot() {
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        Bitmap bitmap = getScreenShot(rootView);
        Uri filePath = store(bitmap, fileName + ".png");
        Toasty.success(getApplicationContext(), "Ticket saved in gallery", Toasty.LENGTH_LONG).show();
    }

    @Override
    public void onPaymentSuccess(String s) {
        Toasty.success(getApplicationContext(), "Booking confirmed", Toasty.LENGTH_LONG).show();

        informationTV.setText("Txn ID: " + s);
        containerError.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);
        fileName = s;

        handler.postDelayed(runnable, 1000);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takeScreenshot();
            }
        }
    }

    private void shareImage(Uri file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "GoTo");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "Download GoTo - The realtime bus booking app.");
        intent.putExtra(Intent.EXTRA_STREAM, file);
        try {
            startActivity(Intent.createChooser(intent, "Share ticket"));
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "No App Available", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getScreenShot(View view) {
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private Uri store(Bitmap bm, String fileName) {
        Uri bmpUri = null;

        try {
            // This way, you don't need to request external read/write permission.
            File file = new File(Environment.getExternalStorageDirectory(), fileName);
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();

        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public void onPaymentError(int i, String s) {
        container.setVisibility(View.GONE);
        containerError.setVisibility(View.VISIBLE);
        Toasty.error(getApplicationContext(), "Payment failed", Toasty.LENGTH_LONG).show();
    }

    private void startPayment() {
        Checkout checkout = new Checkout();

        checkout.setImage(R.drawable.goto_trans);

        final Activity activity = this;

        try {
            JSONObject options = new JSONObject();

            options.put("name", "Goto Bus Pvt Ltd.");

            options.put("description", "Happy Journey");

            options.put("currency", "INR");

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
