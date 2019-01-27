package com.gotobus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private GoogleMap mMap;

    private final LatLng mDefaultLocation = new LatLng(28.7041, 77.1025);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    private Location mLastKnownLocation;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private CameraPosition mCameraPosition;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    MarkerOptions sourceMarkerOption, destinationMarkerOption, busMarker;
    String nearestSourceLat, nearestSourceLong, nearestDestinationLat, nearestDestinationLong;
    MarkerOptions nearestSourceOption, nearestDestinationOption;
    String pickupAddress = "Pickup point";
    String dropAddress = "Drop-off point";
    String[] busSource;
    String[] busDestination;

    int AUTOCOMPLETE_SOURCE = 1, AUTOCOMPLETE_DESTINATION = 2;

    EditText sourceAddress, destinationAddress;

    LinearLayout container, sleeper, ac, volvo, busInfo, bookingOptions;
    String baseUrl;

    String lineColor = "#0fa4e6";

    Button bookNow;

    String busName = "", busNumber = "", busPhone = "", busId = "", routeId = "", busType = "";

    SharedPreferences sharedPreferences;
    String PREFS_NAME = "MyApp_Settings";
    String accessToken;
    SharedPreferences.Editor editor;

    String eta = "";
    int etaValue = 0;
    TextView sleeperETA, acETA, volvoETA;

    ProgressDialog progressDialog;
    Handler handler;
    Runnable getBusLocationRunnable;
    boolean tripCompleted = false;
    Marker currentBusMarker;
    boolean busSelected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //toolbar.setNavigationIcon(R.drawable.ic_toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

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

        handler = new Handler();
        getBusLocationRunnable = new Runnable() {
            @Override
            public void run() {
                getBusLocation(busId);
                if (!tripCompleted) {
                    handler.postDelayed(this, 12000);
                }
            }
        };


        bookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!routeId.equals("") && busSelected) {

                    editor.putString("source", sourceMarkerOption.getPosition().latitude+","+sourceMarkerOption.getPosition().longitude);
                    editor.putString("destination", destinationMarkerOption.getPosition().latitude+","+destinationMarkerOption.getPosition().longitude);

                    editor.putString("source_address", sourceAddress.getText().toString());
                    editor.putString("destination_address", destinationAddress.getText().toString());

                    editor.commit();

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
                } catch (GooglePlayServicesRepairableException e) {
                    Log.e("Exception", e.getMessage());
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.e("Exception", e.getMessage());
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
                    } catch (GooglePlayServicesRepairableException e) {
                        Log.e("Exception", e.getMessage());
                    } catch (GooglePlayServicesNotAvailableException e) {
                        Log.e("Exception", e.getMessage());
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
                } catch (GooglePlayServicesRepairableException e) {
                    Log.e("Exception", e.getMessage());
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.e("Exception", e.getMessage());
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
                    } catch (GooglePlayServicesRepairableException e) {
                        Log.e("Exception", e.getMessage());
                    } catch (GooglePlayServicesNotAvailableException e) {
                        Log.e("Exception", e.getMessage());
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
                    searchBus("Sleeper", true);
                } else {
                    Toast.makeText(getApplicationContext(), "Please select source and destination", Toast.LENGTH_LONG).show();
                }
            }
        });

        ac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sourceMarkerOption != null && destinationMarkerOption != null) {
                    searchBus("AC", true);
                } else {
                    Toast.makeText(getApplicationContext(), "Please select source and destination", Toast.LENGTH_LONG).show();
                }
            }
        });

        volvo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sourceMarkerOption != null && destinationMarkerOption != null) {
                    searchBus("Volvo", true);
                } else {
                    Toast.makeText(getApplicationContext(), "Please select source and destination", Toast.LENGTH_LONG).show();
                }
            }
        });


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

    private void getBusLocation(String busId) {
        AndroidNetworking.post(baseUrl + "/getBusLocation.php")
                .setOkHttpClient(NetworkCookies.okHttpClient)
                .addHeaders("Authorization", accessToken)
                .addBodyParameter("id", busId)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject result = response.getJSONObject("result");
                            boolean success = Boolean.parseBoolean(result.get("success").toString());
                            if (success) {
                                JSONObject data = result.getJSONObject("data");
                                String[] busLocation = data.get("last_location").toString().split(",");
                                LatLng origin = new LatLng(Double.parseDouble(busLocation[0]), Double.parseDouble(busLocation[1]));
                                float bearing = Float.parseFloat(data.get("bearing").toString());
                                busType = data.get("bus_type").toString();
//                                Toast.makeText(getApplicationContext(), busType, Toast.LENGTH_LONG).show();
                                if (busType.equals("Sleeper")) {
                                    busMarker = new MarkerOptions()
                                            .position(origin)
                                            .flat(true)
                                            .rotation(bearing)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.sleeper_bus_marker));
                                } else if (busType.equals("AC")) {
                                    busMarker = new MarkerOptions()
                                            .position(origin)
                                            .flat(true)
                                            .rotation(bearing)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ac_bus_marker));
                                } else if (busType.equals("Volvo")) {
                                    busMarker = new MarkerOptions()
                                            .position(origin)
                                            .flat(true)
                                            .rotation(bearing)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.volvo_bus_marker));
                                }
                                if (currentBusMarker != null) {
                                    currentBusMarker.remove();
                                }
                                currentBusMarker = mMap.addMarker(busMarker);
                            }
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });

    }

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
        String url = "";
        if (booked) {
            url = baseUrl + "/checkBooking.php";
            String[] source = sharedPreferences.getString("source","").split(",");
            String[] destination = sharedPreferences.getString("destination","").split(",");

            sourceAddress.setText(sharedPreferences.getString("source_address", ""));
            destinationAddress.setText(sharedPreferences.getString("destination_address", ""));

            if (sourceMarkerOption==null) {
                sourceMarkerOption = new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(source[0]), Double.parseDouble(source[1])))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.source_pin));
            }
            if (destinationMarkerOption ==null) {
                destinationMarkerOption = new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(destination[0]), Double.parseDouble(destination[1])))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_pin));
            }
        }
        else {
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
                            JSONObject result = response.getJSONObject("result");
                            boolean success = Boolean.parseBoolean(result.get("success").toString());
                            if (success) {
                                JSONObject data = result.getJSONObject("data");
                                nearestSourceLat = data.getJSONArray("neareatSource").get(0).toString();
                                nearestSourceLong = data.getJSONArray("neareatSource").get(1).toString();

                                nearestDestinationLat = data.getJSONArray("nearestDestination").get(0).toString();
                                nearestDestinationLong = data.getJSONArray("nearestDestination").get(1).toString();
                                String waypoints = "";
                                JSONObject route = data.getJSONObject("route");
                                routeId = route.get("id").toString();
                                busId = route.get("bus_id").toString();
                                busName = route.get("name").toString();
                                busPhone = route.get("phone").toString();
                                busNumber = route.get("bus_number").toString();
                                waypoints = route.get("waypoints").toString();
                                busSource = route.get("sourceLatLong").toString().split(",");
                                busDestination = route.get("destinationLatLong").toString().split(",");
                                LatLng origin = new LatLng(Double.parseDouble(busSource[0]), Double.parseDouble(busSource[1]));
                                LatLng dest = new LatLng(Double.parseDouble(busDestination[0]), Double.parseDouble(busDestination[1]));
                                if (!booked) {
                                    setETA(busId, origin, busType);
                                }

                                if (buildRoute) {
                                    progressDialog.hide();
                                    busSelected = true;
                                    mMap.clear();
                                    mMap.addMarker(sourceMarkerOption);
                                    mMap.addMarker(destinationMarkerOption);
//                                    handler.postDelayed(getBusLocationRunnable, 12000);

                                    lineColor = "#0fa4e6";
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
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.goto_bus_pin))
                                            .position(dest);
                                    mMap.addMarker(nearestSourceOption);
                                    waypoints = "";
                                    lineColor = "#00FF00";
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
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.goto_bus_pin))
                                            .position(origin);
                                    mMap.addMarker(nearestDestinationOption);
                                    waypoints = "";
                                    lineColor = "#FF0000";
                                    String url2 = getDirectionsUrl(origin, dest, waypoints);
                                    DownloadTask downloadTask2 = new DownloadTask(lineColor);
                                    downloadTask2.execute(url2);
                                }
                                if (booked) {
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
                                    editor.putString("source", null);
                                    editor.putString("destination", null);
                                    editor.commit();
                                    if (!booked) {
                                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                    }
                                }
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

    private void setETA(String busId, final LatLng dest, final String busType) {
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
                                String[] busLocation = data.get("last_location").toString().split(",");
                                LatLng origin = new LatLng(Double.parseDouble(busLocation[0]), Double.parseDouble(busLocation[1]));
                                float bearing = Float.parseFloat(data.get("bearing").toString());
                                if (busType.equals("Sleeper")) {
                                    busMarker = new MarkerOptions()
                                            .position(origin)
                                            .flat(true)
                                            .rotation(bearing)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.sleeper_bus_marker));
                                } else if (busType.equals("AC")) {
                                    busMarker = new MarkerOptions()
                                            .position(origin)
                                            .flat(true)
                                            .rotation(bearing)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ac_bus_marker));
                                } else if (busType.equals("Volvo")) {
                                    busMarker = new MarkerOptions()
                                            .position(origin)
                                            .flat(true)
                                            .rotation(bearing)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.volvo_bus_marker));
                                }

                                mMap.addMarker(busMarker);

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
                                                            if (busType.equals("Sleeper")) {
                                                                sleeperETA.setText(eta);
                                                            } else if (busType.equals("AC")) {
                                                                acETA.setText(eta);
                                                            } else if (busType.equals("Volvo")) {
                                                                volvoETA.setText(eta);
                                                            }
                                                        } else {
                                                            Toast.makeText(getApplicationContext(), "Unable to calculate ETA", Toast.LENGTH_LONG).show();
                                                        }

                                                    }
                                                } catch (Exception e) {

                                                }
                                            }

                                            @Override
                                            public void onError(ANError anError) {

                                            }
                                        });

                            }
                            else {
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
                Place place = PlacePicker.getPlace(getApplicationContext(), data);
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
                    searchBus("Sleeper", false);
                    searchBus("AC", false);
                    searchBus("Volvo", false);
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
                Place place = PlacePicker.getPlace(getApplicationContext(), data);
                destinationAddress.setText(place.getAddress());

                mMap.clear();

                destinationMarkerOption = new MarkerOptions()
                        .position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_pin));

                mMap.addMarker(destinationMarkerOption);

                if (sourceMarkerOption != null) {
                    mMap.addMarker(sourceMarkerOption);
                    searchBus("Sleeper", false);
                    searchBus("AC", false);
                    searchBus("Volvo", false);
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

        //noinspection SimplifiableIfStatement
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.style_json));

        mMap.setBuildingsEnabled(true);

        // Prompt the user for permission.
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
        if (sharedPreferences.getString("source", null) !=null) {
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
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
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
                        if (task.isSuccessful()) {
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

    private class DownloadTask extends AsyncTask<String, Void, String> {
        String lineColor;

        public DownloadTask(String lineColor) {
            this.lineColor = lineColor;
        }

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
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


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        String lineColor;

        public ParserTask(String lineColor) {
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
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
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

    private String getDirectionsUrl(LatLng origin, LatLng dest, String waypoints) {

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

        parameters += "&waypoints=" + waypoints;
        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


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

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
