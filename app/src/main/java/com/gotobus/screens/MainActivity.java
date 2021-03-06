package com.gotobus.screens;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.gotobus.R;
import com.gotobus.utility.DirectionsJSONParser;
import com.gotobus.utility.Journey;
import com.gotobus.utility.NetworkCookies;
import com.gotobus.utility.ResponseValidator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1, PERMISSION_REQUEST_PHONE_CALL = 2;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private final LatLng mDefaultLocation = new LatLng(28.7041, 77.1025);
    private final int AUTOCOMPLETE_SOURCE = 1;
    private final int AUTOCOMPLETE_DESTINATION = 2;
    private final String busType = "";
    private final String PREFS_NAME = "MyApp_Settings";
    Marker sourceMarker;
    Marker destinationMarker;
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted, mCallPermissionGranted;
    private Location mLastKnownLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private MarkerOptions sourceMarkerOption;
    private MarkerOptions destinationMarkerOption;
    private MarkerOptions busMarker;
    private String nearestSourceLat;
    private String nearestSourceLong;
    private String nearestDestinationLat;
    private String nearestDestinationLong;
    private MarkerOptions nearestSourceOption;
    private MarkerOptions nearestDestinationOption;
    private String pickupAddress = "Pickup point";
    private String dropAddress = "Drop-off point";
    private String[] busSource;
    private String[] busDestination;
    private EditText sourceAddress;
    private EditText destinationAddress;
    private LinearLayout container;
    private LinearLayout sleeper;
    private LinearLayout ac;
    private LinearLayout volvo;
    private LinearLayout busInfo;
    private LinearLayout bookingOptions;
    private String baseUrl;
    private String lineColor = "#0fa4e6";
    private Button bookNow;
    private Button viewMore;
    private String busName = "";
    private String busNumber = "";
    private String busPhone = "";
    private String busId = "";
    private String routeId = "";
    private String bookedBusType = "";
    private String otp = "";
    private String fare = "";
    private SharedPreferences sharedPreferences;
    private String accessToken;
    private SharedPreferences.Editor editor;
    private String eta = "";
    private int etaValue = 0;
    private TextView sleeperETA;
    private TextView acETA;
    private TextView volvoETA;
    private TextView cancelBus;
    private ProgressDialog progressDialog;
    private Handler handler;
    private Runnable getBusLocationRunnable;
    private boolean tripCompleted = false;
    private Marker currentBusMarker;
    private boolean busSelected = false;

    private TextView busNameView;
    private TextView busNumberView;
    private TextView fareView;
    private TextView otpView;
    private TextView etaView;
    private ImageView callBus;
    private Runnable adjustZoomLevel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            CameraPosition mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

//        getSupportActionBar().setDisplayShowTitleEnabled(true);
//        //toolbar.setNavigationIcon(R.drawable.ic_toolbar);
//        toolbar.setTitle("");
//        toolbar.setSubtitle("");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        baseUrl = getResources().getString(R.string.base_url);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        accessToken = sharedPreferences.getString("access_token", null);


        AndroidNetworking.initialize(getApplicationContext());


        container = findViewById(R.id.container);
        container.requestFocus();
        busInfo = findViewById(R.id.bus_info);
        bookingOptions = findViewById(R.id.booking_options);

        sleeperETA = findViewById(R.id.sleeper_eta);
        acETA = findViewById(R.id.ac_eta);
        volvoETA = findViewById(R.id.volvo_eta);
        sourceAddress = findViewById(R.id.source_address);
        destinationAddress = findViewById(R.id.destination_address);
        bookNow = findViewById(R.id.book_now);
        viewMore = findViewById(R.id.view_more);
        busNameView = findViewById(R.id.bus_name);
        busNumberView = findViewById(R.id.bus_number);
        fareView = findViewById(R.id.fare);
        otpView = findViewById(R.id.otp);
        etaView = findViewById(R.id.booked_bus_eta);
        callBus = findViewById(R.id.call_bus);
        cancelBus = findViewById(R.id.cancel);

        handler = new Handler();
        getBusLocationRunnable = new Runnable() {
            @Override
            public void run() {
//                getBusLocation(busId);
                setETA(busId, new LatLng(Double.parseDouble(nearestSourceLat), Double.parseDouble(nearestSourceLong)), bookedBusType, true);
                if (!tripCompleted) {
                    handler.postDelayed(this, 120000);
                }
            }
        };


        cancelBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Cancel booking?")
                        .setMessage("Are you sure you want to cancel this booking?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                cancelBooking();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();

            }
        });

        viewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewMoreActivity.class);
                if (destinationMarkerOption != null && sourceMarkerOption != null) {

                    Journey.sourceLat = String.valueOf(sourceMarkerOption.getPosition().latitude);
                    Journey.sourceLng = String.valueOf(sourceMarkerOption.getPosition().longitude);
                    Journey.destinationLat = String.valueOf(destinationMarkerOption.getPosition().latitude);
                    Journey.destinationLng = String.valueOf(destinationMarkerOption.getPosition().longitude);
//
                    startActivity(intent);
                } else {
                    Toasty.error(getApplicationContext(), "Please select source and destination", Toasty.LENGTH_LONG).show();
                }

            }
        });

        bookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!routeId.equals("") && busSelected) {

                    editor.putString("source", sourceMarkerOption.getPosition().latitude + "," + sourceMarkerOption.getPosition().longitude);
                    editor.putString("destination", destinationMarkerOption.getPosition().latitude + "," + destinationMarkerOption.getPosition().longitude);

                    editor.putString("source_address", sourceAddress.getText().toString());
                    editor.putString("destination_address", destinationAddress.getText().toString());

                    editor.apply();

                    Intent intent = new Intent(getApplicationContext(), BookBusActivity.class);
                    intent.putExtra("name", busName);
                    intent.putExtra("bus_number", busNumber);
                    intent.putExtra("phone", busPhone);
                    intent.putExtra("source", pickupAddress);
                    intent.putExtra("destination", dropAddress);
                    intent.putExtra("bus_id", busId);
                    intent.putExtra("route_id", routeId);
                    intent.putExtra("eta", etaValue);
                    intent.putExtra("nearestSourceLat", nearestSourceLat);
                    intent.putExtra("nearestSourceLong", nearestSourceLong);
                    intent.putExtra("nearestDestinationLat", nearestDestinationLat);
                    intent.putExtra("nearestDestinationLong", nearestDestinationLong);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Please select a bus first", Toast.LENGTH_LONG).show();
                }
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching routes...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);

        sourceAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    startActivityForResult(builder.build(MainActivity.this), AUTOCOMPLETE_SOURCE);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

        sourceAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    try {
                        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                        startActivityForResult(builder.build(MainActivity.this), AUTOCOMPLETE_SOURCE);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


        destinationAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    startActivityForResult(builder.build(MainActivity.this), AUTOCOMPLETE_DESTINATION);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

        destinationAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    try {
                        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                        startActivityForResult(builder.build(MainActivity.this), AUTOCOMPLETE_DESTINATION);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        sleeper = findViewById(R.id.sleeper);
        ac = findViewById(R.id.ac);
        volvo = findViewById(R.id.volvo);

        sleeper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sourceMarkerOption != null && destinationMarkerOption != null) {
                    if (sleeperETA.getText().equals("---")) {
                        Toast.makeText(getApplicationContext(), "Not available", Toast.LENGTH_LONG).show();
                    } else {
                        searchBus("Sleeper", true);
                        highlightSelectedOption(sleeper);
                        sleeper.setBackgroundColor(Color.LTGRAY);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please select source and destination", Toast.LENGTH_LONG).show();
                }
            }
        });

        ac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sourceMarkerOption != null && destinationMarkerOption != null) {
                    if (acETA.getText().equals("---")) {
                        Toast.makeText(getApplicationContext(), "Not available", Toast.LENGTH_LONG).show();
                    } else {
                        searchBus("AC", true);
                        highlightSelectedOption(ac);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please select source and destination", Toast.LENGTH_LONG).show();
                }
            }
        });

        volvo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sourceMarkerOption != null && destinationMarkerOption != null) {
                    if (volvoETA.getText().equals("---")) {
                        Toast.makeText(getApplicationContext(), "Not available", Toast.LENGTH_LONG).show();
                    } else {
                        searchBus("Volvo", true);
                        highlightSelectedOption(volvo);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please select source and destination", Toast.LENGTH_LONG).show();
                }
            }
        });


        adjustZoomLevel = new Runnable() {
            @Override
            public void run() {
                searchBus("Sleeper", false);
                searchBus("AC", false);
                searchBus("Volvo", false);
            }
        };

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toolbar.setTitleTextColor(Color.BLACK);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void highlightSelectedOption(LinearLayout view) {
        unhighlightAllOptions();
        view.setBackgroundColor(Color.LTGRAY);
    }

    private void unhighlightAllOptions() {
        ac.setBackgroundColor(Color.TRANSPARENT);
        sleeper.setBackgroundColor(Color.TRANSPARENT);
        volvo.setBackgroundColor(Color.TRANSPARENT);
    }

    private void cancelBooking() {
        progressDialog.setMessage("Cancelling...");
        progressDialog.show();
        AndroidNetworking.post(baseUrl + "/cancelBooking.php")
                .setOkHttpClient(NetworkCookies.okHttpClient)
                .addHeaders("Authorization", accessToken)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject result = response.getJSONObject("result");
                            boolean success = Boolean.parseBoolean(result.get("success").toString());
                            if (success) {
                                sourceAddress.setEnabled(true);
                                destinationAddress.setEnabled(true);
                                bookingOptions.setVisibility(View.VISIBLE);
                                busInfo.setVisibility(View.GONE);
                                tripCompleted = true;
                                editor.putString("source", null);
                                editor.putString("destination", null);
                                editor.commit();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                String message = result.get("message").toString();
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            }
                            progressDialog.hide();
                        } catch (Exception e) {
                            progressDialog.hide();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        progressDialog.hide();
                    }
                });
    }

//    private void getBusLocation(final String busId) {
//        AndroidNetworking.post(baseUrl + "/getBusLocation.php")
//                .setOkHttpClient(NetworkCookies.okHttpClient)
//                .addHeaders("Authorization", accessToken)
//                .addBodyParameter("id", busId)
//                .setPriority(Priority.MEDIUM)
//                .build()
//                .getAsJSONObject(new JSONObjectRequestListener() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            JSONObject result = response.getJSONObject("result");
//                            boolean success = Boolean.parseBoolean(result.get("success").toString());
//                            if (success) {
//                                JSONObject data = result.getJSONObject("data");
//                                String[] busLocation = data.get("last_location").toString().split(",");
//                                LatLng origin = new LatLng(Double.parseDouble(busLocation[0]), Double.parseDouble(busLocation[1]));
//                                float bearing = Float.parseFloat(data.get("bearing").toString());
//                                busType = data.get("bus_type").toString();
////                                Toast.makeText(getApplicationContext(), busType, Toast.LENGTH_LONG).show();
//                                if (busType.equals("Sleeper")) {
//                                    busMarker = new MarkerOptions()
//                                            .position(origin)
//                                            .flat(true)
//                                            .rotation(bearing)
//                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.sleeper_bus_marker));
//                                } else if (busType.equals("AC")) {
//                                    busMarker = new MarkerOptions()
//                                            .position(origin)
//                                            .flat(true)
//                                            .rotation(bearing)
//                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ac_bus_marker));
//                                } else if (busType.equals("Volvo")) {
//                                    busMarker = new MarkerOptions()
//                                            .position(origin)
//                                            .flat(true)
//                                            .rotation(bearing)
//                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.volvo_bus_marker));
//                                }
//                                if (currentBusMarker != null) {
//                                    currentBusMarker.remove();
//                                }
//                                currentBusMarker = mMap.addMarker(busMarker);
//                            }
//                        } catch (Exception e) {
//
//                        }
//                    }
//
//                    @Override
//                    public void onError(ANError anError) {
//
//                    }
//                });
//
//    }

    private void searchBus(final String busType, final boolean buildRoute) {
        searchBus(busType, buildRoute, false);
    }

    private void searchBus(final String busType, final boolean buildRoute, final boolean booked) {
        if (!buildRoute) {
            progressDialog.setMessage("Fetching ETA...");
        } else {
            progressDialog.setMessage("Fetching routes...");
        }
        progressDialog.show();
        mMap.clear();
        String url;
        if (booked) {
            url = baseUrl + "/checkBooking.php";
            String[] source = Objects.requireNonNull(sharedPreferences.getString("source", "")).split(",");
            String[] destination = Objects.requireNonNull(sharedPreferences.getString("destination", "")).split(",");

            sourceAddress.setText(sharedPreferences.getString("source_address", ""));
            destinationAddress.setText(sharedPreferences.getString("destination_address", ""));

            if (sourceMarkerOption == null) {
                sourceMarkerOption = new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(source[0]), Double.parseDouble(source[1])))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.source_pin));
            }
            if (destinationMarkerOption == null) {
                destinationMarkerOption = new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(destination[0]), Double.parseDouble(destination[1])))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_pin));
            }
        } else {
            unhighlightAllOptions();
            url = baseUrl + "/search.php";
        }
        mMap.addMarker(sourceMarkerOption);
        mMap.addMarker(destinationMarkerOption);

        AndroidNetworking.post(url)
                .setOkHttpClient(NetworkCookies.okHttpClient)
                .addHeaders("Authorization", accessToken)
                .addBodyParameter("slat", String.valueOf(sourceMarkerOption.getPosition().latitude))
                .addBodyParameter("slong", String.valueOf(sourceMarkerOption.getPosition().longitude))
                .addBodyParameter("dlat", String.valueOf(destinationMarkerOption.getPosition().latitude))
                .addBodyParameter("dlong", String.valueOf(destinationMarkerOption.getPosition().longitude))
                .addBodyParameter("type", busType)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            ResponseValidator.validate(MainActivity.this, response);
                            JSONObject result = response.getJSONObject("result");
                            boolean success = Boolean.parseBoolean(result.get("success").toString());
                            if (success) {
                                JSONObject data = result.getJSONObject("data");
                                nearestSourceLat = data.getJSONArray("neareatSource").get(0).toString();
                                nearestSourceLong = data.getJSONArray("neareatSource").get(1).toString();

                                nearestDestinationLat = data.getJSONArray("nearestDestination").get(0).toString();
                                nearestDestinationLong = data.getJSONArray("nearestDestination").get(1).toString();
                                String waypoints;
                                otp = data.get("otp").toString();
                                fare = data.get("fare").toString();
                                JSONObject route = data.getJSONObject("route");
                                routeId = route.get("id").toString();
                                busId = route.get("bus_id").toString();
                                busName = route.get("name").toString();
                                busPhone = route.get("phone").toString();
                                busNumber = route.get("bus_number").toString();
                                waypoints = route.get("waypoints").toString();
//
                                busSource = route.get("sourceLatLong").toString().split(",");
                                busDestination = route.get("destinationLatLong").toString().split(",");
                                LatLng origin = new LatLng(Double.parseDouble(busSource[0]), Double.parseDouble(busSource[1]));
                                LatLng dest = new LatLng(Double.parseDouble(busDestination[0]), Double.parseDouble(busDestination[1]));
                                if (!booked) {
                                    setETA(busId, new LatLng(Double.parseDouble(nearestSourceLat), Double.parseDouble(nearestSourceLong)), busType, false);
                                }

                                if (buildRoute) {
                                    progressDialog.hide();
                                    busSelected = true;
                                    mMap.clear();
                                    mMap.addMarker(sourceMarkerOption);
                                    mMap.addMarker(destinationMarkerOption);
//                                    handler.postDelayed(getBusLocationRunnable, 12000);

//                                    lineColor = "#0fa4e6";
                                    lineColor = "#000000";

                                    String url = getDirectionsUrl(origin, dest, waypoints);
                                    DownloadTask downloadTask = new DownloadTask(lineColor);
                                    downloadTask.execute(url);

                                    origin = sourceMarkerOption.getPosition();
                                    dest = new LatLng(Double.parseDouble(nearestSourceLat), Double.parseDouble(nearestSourceLong));
                                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                    try {
                                        List<Address> addresses = geocoder.getFromLocation(dest.latitude, dest.longitude, 1);
                                        Address address = addresses.get(0);
                                        pickupAddress = address.getAddressLine(0);
                                    } catch (Exception e) {
                                        Log.e("Exception", e.getMessage());
                                    }
                                    nearestSourceOption = new MarkerOptions()
                                            .title(pickupAddress)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup_marker))
                                            .position(dest);
                                    mMap.addMarker(nearestSourceOption);
                                    waypoints = "";
//                                    lineColor = "#00FF00";
                                    lineColor = "#007c0e";
                                    String url1 = getDirectionsUrl(origin, dest, waypoints);
                                    DownloadTask downloadTask1 = new DownloadTask(lineColor);
                                    downloadTask1.execute(url1);

                                    origin = new LatLng(Double.parseDouble(nearestDestinationLat), Double.parseDouble(nearestDestinationLong));
                                    dest = destinationMarkerOption.getPosition();

                                    try {
                                        List<Address> addresses = geocoder.getFromLocation(origin.latitude, origin.longitude, 1);
                                        Address address = addresses.get(0);
                                        dropAddress = address.getAddressLine(0);
                                    } catch (Exception e) {
                                        Log.e("Exception", e.getMessage());
                                    }
                                    nearestDestinationOption = new MarkerOptions()
                                            .title(dropAddress)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.dropoff_marker))
                                            .position(origin);
                                    mMap.addMarker(nearestDestinationOption);
                                    waypoints = "";
//                                    lineColor = "#FF0000";
                                    lineColor = "#ad0000";
                                    String url2 = getDirectionsUrl(origin, dest, waypoints);
                                    DownloadTask downloadTask2 = new DownloadTask(lineColor);
                                    downloadTask2.execute(url2);
                                }
                                if (booked) {
                                    busNameView.setText(busName);
                                    busNumberView.setText(busNumber);
                                    fareView.setText("Rs. " + fare);
                                    otpView.setText("OTP : " + otp);
                                    callBus.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(Intent.ACTION_CALL);
                                            intent.setData(Uri.parse("tel:" + busPhone));
                                            if (mCallPermissionGranted) {
                                                startActivity(intent);
                                            } else {
                                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_PHONE_CALL);
                                            }
                                        }
                                    });
                                    sourceAddress.setEnabled(false);
                                    destinationAddress.setEnabled(false);
                                    bookingOptions.setVisibility(View.GONE);
                                    busInfo.setVisibility(View.VISIBLE);
                                    handler.postDelayed(getBusLocationRunnable, 1000);
                                }

                            } else {
                                progressDialog.hide();
                                String message = result.get("message").toString();
                                if (message.equals("Invalid access token.")) {
                                    editor.putString("access_token", null);
                                    editor.commit();
                                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                if (buildRoute) {
                                    if (!booked) {
                                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                    }
                                }
                                editor.putString("source", null);
                                editor.putString("destination", null);
                                editor.commit();
                            }
                        } catch (JSONException e) {
                            progressDialog.hide();
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Toast.makeText(getApplicationContext(), error.getErrorBody(), Toast.LENGTH_LONG).show();
                        progressDialog.hide();
                    }
                });
    }

    private void setETA(String busId, final LatLng dest, final String busType, final boolean booked) {
        AndroidNetworking.post(baseUrl + "/getBusLocation.php")
                .setOkHttpClient(NetworkCookies.okHttpClient)
                .addHeaders("Authorization", accessToken)
                .addBodyParameter("id", busId)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.hide();
                        try {
                            JSONObject result = response.getJSONObject("result");
                            boolean success = Boolean.parseBoolean(result.get("success").toString());
                            if (success) {
                                JSONObject data = result.getJSONObject("data");
                                bookedBusType = data.get("bus_type").toString();
                                String[] busLocation = data.get("last_location").toString().split(",");
                                LatLng origin = new LatLng(Double.parseDouble(busLocation[0]), Double.parseDouble(busLocation[1]));
                                float bearing = Float.parseFloat(data.get("bearing").toString());
                                switch (busType) {
                                    case "Sleeper":
                                        busMarker = new MarkerOptions()
                                                .position(origin)
                                                .flat(true)
                                                .rotation(bearing)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.sleeper_bus_marker));
                                        break;
                                    case "AC":
                                        busMarker = new MarkerOptions()
                                                .position(origin)
                                                .flat(true)
                                                .rotation(bearing)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ac_bus_marker));
                                        break;
                                    case "Volvo":
                                        busMarker = new MarkerOptions()
                                                .position(origin)
                                                .flat(true)
                                                .rotation(bearing)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.volvo_bus_marker));
                                        break;
                                }


                                if (booked) {
                                    if (currentBusMarker != null) {
                                        currentBusMarker.remove();
                                    }
                                }
                                if (busMarker != null) {
                                    currentBusMarker = mMap.addMarker(busMarker);
                                }

                                String url = getDirectionsUrl(origin, dest, "");


                                AndroidNetworking.get(url)
                                        .setOkHttpClient(NetworkCookies.okHttpClient)
                                        .build()
                                        .getAsJSONObject(new JSONObjectRequestListener() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                try {
                                                    JSONArray result = response.getJSONArray("routes");
                                                    if (result.length() > 0) {
                                                        JSONObject route = result.getJSONObject(0);
                                                        JSONArray legs = route.getJSONArray("legs");

                                                        if (legs.length() > 0) {
                                                            eta = legs.getJSONObject(0).getJSONObject("duration").get("text").toString();
                                                            etaValue = Integer.parseInt(legs.getJSONObject(0).getJSONObject("duration").get("value").toString());
                                                            switch (busType) {
                                                                case "Sleeper":
                                                                    sleeperETA.setText(eta);
                                                                    break;
                                                                case "AC":
                                                                    acETA.setText(eta);
                                                                    break;
                                                                case "Volvo":
                                                                    volvoETA.setText(eta);
                                                                    break;
                                                            }

//                                                            Log.d("route", String.valueOf(response));
                                                            etaView.setText("ETA : " + eta);
//                                                            Log.d("agla", "agla");

                                                        } else {
                                                            Toast.makeText(getApplicationContext(), "Unable to calculate ETA", Toast.LENGTH_LONG).show();
                                                        }

                                                    }
                                                } catch (Exception e) {
                                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }

                                            @Override
                                            public void onError(ANError anError) {

                                            }
                                        });

                            } else {
                                String message = result.get("message").toString();
                                if (message.equals("Invalid access token.")) {
                                    editor.putString("access_token", null);
                                    editor.commit();
                                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            }

                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        progressDialog.hide();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_SOURCE) {
            if (resultCode == RESULT_OK) {
                busSelected = false;
                Place place = PlacePicker.getPlace(getApplicationContext(), Objects.requireNonNull(data));
                sourceAddress.setText(place.getAddress());

                mMap.clear();

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(place.getLatLng())
                        .zoom(DEFAULT_ZOOM)
                        .build();

                sourceMarkerOption = new MarkerOptions()
                        .position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.source_pin));

                mMap.addMarker(sourceMarkerOption);

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                if (destinationMarkerOption != null) {
                    mMap.addMarker(destinationMarkerOption);

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    builder.include(sourceMarkerOption.getPosition());
                    builder.include(destinationMarkerOption.getPosition());

                    LatLngBounds bounds = builder.build();
                    int padding = 300; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.animateCamera(cu);

                    handler.postDelayed(adjustZoomLevel, 1200);
//                    // Getting URL to the Google Directions API
//                    LatLng origin = sourceMarkerOption.getPosition();
//                    LatLng dest = destinationMarkerOption.getPosition();
//                    String waypoints = "";

//                    try {
//                        String url = getDirectionsUrl(origin, dest, waypoints);
//                        DownloadTask downloadTask = new DownloadTask();
//                        downloadTask.execute(url);
//                    }
//                    catch (Exception e) {
//                        Log.d("Route exception" , e.getMessage());
//                    }
                }
            }
        } else if (requestCode == AUTOCOMPLETE_DESTINATION) {
            if (resultCode == RESULT_OK) {
                busSelected = false;
                Place place = PlacePicker.getPlace(getApplicationContext(), Objects.requireNonNull(data));
                destinationAddress.setText(place.getAddress());

                mMap.clear();

                destinationMarkerOption = new MarkerOptions()
                        .position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_pin));

                mMap.addMarker(destinationMarkerOption);

                if (sourceMarkerOption != null) {
                    mMap.addMarker(sourceMarkerOption);

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    builder.include(sourceMarkerOption.getPosition());
                    builder.include(destinationMarkerOption.getPosition());

                    LatLngBounds bounds = builder.build();
                    int padding = 300; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.animateCamera(cu);


                    handler.postDelayed(adjustZoomLevel, 1200);
//                    // Getting URL to the Google Directions API
//                    LatLng origin = sourceMarkerOption.getPosition();
//                    LatLng dest = destinationMarkerOption.getPosition();
//                    String waypoints = "";
//
//                    try {
//                        String url = getDirectionsUrl(origin, dest, waypoints);
//                        DownloadTask downloadTask = new DownloadTask();
//                        downloadTask.execute(url);
//                    }
//                    catch (Exception e) {
//                        Log.d("Route exception" , e.getMessage());
//                    }
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.profile) {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.trips) {
            Intent intent = new Intent(getApplicationContext(), TripsActivity.class);
            startActivity(intent);
        }
//        else if (id == R.id.locate) {
//            Intent intent = new Intent(getApplicationContext(), LocateActivity.class);
//            startActivity(intent);
//        }
        else if (id == R.id.wallet) {
            Intent intent = new Intent(getApplicationContext(), WalletActivity.class);
            startActivity(intent);
        } else if (id == R.id.help) {

        } else if (id == R.id.share) {

        } else if (id == R.id.logout) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.style_json));

        mMap.setBuildingsEnabled(true);

        // Prompt the user for permission.
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
        if (sharedPreferences.getString("source", null) != null) {
            searchBus(busType, true, true);
        }

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        mCallPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
                break;
            }
            case PERMISSION_REQUEST_PHONE_CALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCallPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        if (sourceAddress.getText().toString().length() == 0) {
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), 1);
                                Address address = addresses.get(0);
                                String currentAddress = address.getAddressLine(0);
                                sourceAddress.setText(currentAddress);
                            } catch (Exception e) {
                                Log.e("Exception", e.getMessage());
                            }
                        }

                        return false;
                    }
                });

                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse("http://maps.google.com/maps?daddr=" + marker.getPosition().latitude + "," + marker.getPosition().longitude));
                        startActivity(intent);
                    }
                });

            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();

                            sourceMarkerOption = new MarkerOptions()
                                    .position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.source_pin));
                            mMap.addMarker(sourceMarkerOption);

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation
                                                    .getLongitude()), DEFAULT_ZOOM));
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), 1);
                                Address address = addresses.get(0);
                                String currentAddress = address.getAddressLine(0);
                                sourceAddress.setText(currentAddress);
                            } catch (Exception e) {
                                Log.e("Exception", e.getMessage());
                            }
                        } else {
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest, String waypoints) {

        Log.d("waypoints_url", "getDirectionsUrl: " + waypoints);

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        String apiKey = "key=" + getResources().getString(R.string.google_maps_key);
        String callback = "callback=initialize";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&" + apiKey + "&" + callback;

        Log.d("waypoints_url", "getDirectionsUrl: " + waypoints);

        parameters += "&waypoints=" + waypoints;
        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        url = url.replaceAll(" ", "%20");

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Download Exception", e.toString());
        } finally {
            Objects.requireNonNull(iStream).close();
            Objects.requireNonNull(urlConnection).disconnect();
        }
        return data;
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        final String lineColor;

        ParserTask(String lineColor) {
            this.lineColor = lineColor;
        }

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            if (result == null) {
                return;
            }
            ArrayList points;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                    double lng = Double.parseDouble(Objects.requireNonNull(point.get("lng")));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(18);
                lineOptions.color(Color.parseColor(lineColor));
                lineOptions.geodesic(true);

            }

// Drawing polyline in the Google Map for the i-th route
            try {
                mMap.addPolyline(lineOptions);
            } catch (Exception e) {
                Log.d("Polyline", e.getMessage());
            }
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        final String lineColor;

        DownloadTask(String lineColor) {
            this.lineColor = lineColor;
        }

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                Log.d("download_url", url[0]);
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask(lineColor);
            parserTask.execute(result);

        }
    }
}
