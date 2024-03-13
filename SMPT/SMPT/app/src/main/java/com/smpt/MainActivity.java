package com.smpt;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MyAsyncTask.AsyncResponse, GoogleMap.OnMarkerClickListener {

    private final int FINE_LOCATION_PERMISSION_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean myLocationButtonClicked = false;
    private Location lastKnownLocation;
    private boolean isLocationUpdateRequested = false;
    private boolean isTrackingLocation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        } else {
            createLocationRequest();
            createLocationCallback();
            getLastKnownLocationAndFetchPlaces();
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                }
                createLocationRequest();
                createLocationCallback();
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission is denied, please allow the permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && isTrackingLocation) {
                    for (Location location : locationResult.getLocations()) {
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(location.getLatitude(), location.getLongitude())) // Ustawia pozycję
                                .zoom(18) // Ustawia poziom zoomu
                                .tilt(60) // Ustawia kąt nachylenia
                                .build(); // Buduje nową pozycję kamery
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                }
            }
        };
    }

    private void getLastKnownLocationAndFetchPlaces() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Pobieranie lokalizacji...", Toast.LENGTH_SHORT).show();

            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            lastKnownLocation = location;
                            if (myLocationButtonClicked) {
                                moveToLocationAndFetchPlaces(new LatLng(location.getLatitude(), location.getLongitude()));
                                myLocationButtonClicked = false;
                            } else {
                                if (!isLocationUpdateRequested) {
                                    Log.d("MainActivity", "Uruchamianie MyAsyncTask do pobrania danych z Google Places API.");
                                    new MyAsyncTask(location.getLatitude(), location.getLongitude(), MainActivity.this).execute();
                                    isLocationUpdateRequested = true;
                                }
                            }
                        } else {
                            Log.d("MainActivity", "Nie można uzyskać ostatniej lokalizacji.");
                        }
                    });
        }
    }

    private void moveToLocationAndFetchPlaces(LatLng latLng) {
        if (mMap != null && latLng != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(18)
                    .tilt(60)
                    .build();

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            mMap.setOnMyLocationButtonClickListener(() -> {
                isTrackingLocation = true;
                if (lastKnownLocation != null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
                            .zoom(18)
                            .tilt(60)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
                startLocationUpdates();
                return true;
            });

            mMap.setOnCameraMoveStartedListener(reason -> {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    isTrackingLocation = false;
                }
            });

            mMap.setOnMarkerClickListener(this);
        } else {
            requestLocationPermission();
        }
    }

    @Override
    public void processFinish(ArrayList<Place2> output) {
        int numberOfPlaces = output.size();
        Toast.makeText(this, "Pobrano " + numberOfPlaces + " lokalizacji z bazy danych", Toast.LENGTH_SHORT).show();

        for (Place2 place : output) {
            LatLng placeLocation = new LatLng(place.getLatitude(), place.getLongitude());
            String photoUrl = place.getMainImageUrl();

            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                        .asBitmap()
                        .load(photoUrl)
                        .override(300, 300) // Ustawia stały rozmiar obrazka na 100x100 pikseli
                        .circleCrop() // Przycina obrazek do kształtu koła
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(placeLocation)
                                        .icon(BitmapDescriptorFactory.fromBitmap(resource)) // Ustaw przetworzony obrazek jako ikonę
                                        .title(place.getTitle()));
                                marker.setTag(place);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }
                        });
            } else {
                Marker marker = mMap.addMarker(new MarkerOptions().position(placeLocation).title(place.getTitle()));
                marker.setTag(place); // Przypisanie obiektu Place jako tag markera
            }
        }

        startLocationUpdates();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Place2 place = (Place2) marker.getTag();
        if (place != null) {
            Log.d("MainActivity", "Marker clicked: " + place.getTitle());
            showPlaceDetailsFragment(place);
            Toast.makeText(this, "Wybrano: " + place.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void showPlaceDetailsFragment(Place2 place) {
        LocationDetailsFragment fragment = LocationDetailsFragment.newInstance(place);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}