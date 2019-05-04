package com.gotobus.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.gotobus.R;
import com.gotobus.utility.CustomMapUtils;
import com.gotobus.utility.NetworkCookies;
import com.gotobus.utility.ResponseValidator;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import static com.gotobus.utility.Journey.destinationLat;
import static com.gotobus.utility.Journey.destinationLng;
import static com.gotobus.utility.Journey.sourceLat;
import static com.gotobus.utility.Journey.sourceLng;

public class SelectedBusActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1, PERMISSION_REQUEST_PHONE_CALL = 2;
    private final LatLng mDefaultLocation = new LatLng(28.7041, 77.1025);
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted, mCallPermissionGranted;
    private Location mLastKnownLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private CameraPosition mCameraPosition;

    Button continueToSeatSelection;
    TextView title, type, fare, arrivalTime, departureTime, pickupPoint, dropoffPoint;
    String routeId, busName, busType, busFare, busArrivalTime, busDepartureTime, source, destination;
    CustomMapUtils customMapUtils;
    String baseUrl;
    MarkerOptions pickupMarkerOptions, dropoffMarkerOptions;
    Double pickupLat, pickupLng, dropoffLat, dropoffLng;
    LatLng pickupLatLng, dropoffLatLng;
    String pickupAddress, dropoffAddress;

    SharedPreferences sharedPreferences;
    String PREFS_NAME = "MyApp_Settings";
    String accessToken;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_bus);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        baseUrl = getResources().getString(R.string.base_url);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        accessToken = sharedPreferences.getString("access_token", null);


        customMapUtils = new CustomMapUtils(getApplicationContext());

        title = findViewById(R.id.title);
        type = findViewById(R.id.type);
        fare = findViewById(R.id.fare);
        arrivalTime = findViewById(R.id.arrival_time);
        departureTime = findViewById(R.id.departure_time);
        pickupPoint = findViewById(R.id.pickup_point);
        dropoffPoint = findViewById(R.id.dropoff_point);


        routeId = getIntent().getExtras().get("route_id").toString();

        busName = getIntent().getExtras().get("bus_name").toString();
        title.setText(busName);

        busType = getIntent().getExtras().get("bus_type").toString();
        type.setText(busType);

        busFare = getIntent().getExtras().get("fare").toString();
        fare.setText("₹ " + busFare);

        busArrivalTime = getIntent().getExtras().get("arrival_time").toString();
        arrivalTime.setText(busArrivalTime);

        busDepartureTime = getIntent().getExtras().get("departure_time").toString();
        arrivalTime.setText(busArrivalTime);


        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        continueToSeatSelection = findViewById(R.id.continue_to_seat_selection);
        continueToSeatSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SeatSelectionActivity.class);
                String busName = getIntent().getExtras().get("bus_name").toString();
                intent.putExtra("bus_name", busName);
                startActivity(intent);
            }
        });

        searchBus();
    }

    private void searchBus() {
        AndroidNetworking.post(baseUrl + "/getNearestWaypoints.php")
                .setOkHttpClient(NetworkCookies.okHttpClient)
                .addHeaders("Authorization", accessToken)
                .addBodyParameter("sourceLat", sourceLat)
                .addBodyParameter("sourceLng", sourceLng)
                .addBodyParameter("destinationLat", destinationLat)
                .addBodyParameter("destinationLng", destinationLng)
                .addBodyParameter("routeId", routeId)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (ResponseValidator.validate(SelectedBusActivity.this, response)) {
                                JSONObject result = response.getJSONObject("result");
                                boolean success = Boolean.parseBoolean(result.get("success").toString());
                                if (success) {
                                    JSONObject data = result.getJSONObject("data");
                                    pickupLat = Double.parseDouble(data.getJSONArray("neareatSource").get(0).toString());
                                    pickupLng = Double.parseDouble(data.getJSONArray("neareatSource").get(1).toString());
                                    pickupLatLng = new LatLng(pickupLat, pickupLng);

                                    dropoffLat = Double.parseDouble(data.getJSONArray("nearestDestination").get(0).toString());
                                    dropoffLng = Double.parseDouble(data.getJSONArray("nearestDestination").get(1).toString());
                                    dropoffLatLng = new LatLng(dropoffLat, dropoffLng);

                                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                    try {
                                        List<Address> addresses = geocoder.getFromLocation(pickupLatLng.latitude, pickupLatLng.longitude, 1);
                                        Address address = addresses.get(0);
                                        pickupAddress = address.getAddressLine(0);
                                        pickupPoint.setText(pickupAddress);

                                    } catch (Exception e) {
                                        Log.e("Exception", e.getMessage());
                                    }

                                    try {
                                        List<Address> addresses = geocoder.getFromLocation(dropoffLatLng.latitude, dropoffLatLng.longitude, 1);
                                        Address address = addresses.get(0);
                                        dropoffAddress = address.getAddressLine(0);
                                        dropoffPoint.setText(dropoffAddress);

                                    } catch (Exception e) {
                                        Log.e("Exception", e.getMessage());
                                    }

                                    pickupMarkerOptions = new MarkerOptions()
                                            .title("Pickup Location")
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup_marker))
                                            .position(pickupLatLng);
                                    mMap.addMarker(pickupMarkerOptions);

                                    dropoffMarkerOptions = new MarkerOptions()
                                            .title("Pickup Location")
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.dropoff_marker))
                                            .position(dropoffLatLng);
                                    mMap.addMarker(dropoffMarkerOptions);

                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                                    builder.include(pickupMarkerOptions.getPosition());
                                    builder.include(dropoffMarkerOptions.getPosition());


                                    LatLngBounds bounds = builder.build();
                                    int padding = 300; // offset from edges of the map in pixels
                                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                    mMap.animateCamera(cu);

                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.style_json));

        mMap.setBuildingsEnabled(true);

        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
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

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation
                                                    .getLongitude()), DEFAULT_ZOOM));
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

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

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

}
