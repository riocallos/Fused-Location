package com.riocallos.fusedlocation;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Map;

public class MapActivity extends AppCompatActivity implements FusedLocationProvider.LocationResultCallback {

    private String TAG = MapActivity.class.getSimpleName();

    private MapView mapView;

    private MapController mapController;

    private FusedLocationProvider fusedLocationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        mapController = (MapController) mapView.getController();

        checkGooglePlayServices();

    }

    @Override
    public void onResume(){

        super.onResume();
        //test

        mapView.onResume();

    }

    @Override
    public void onPause() {

        super.onPause();

        mapView.onPause();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        initializeGoogleApiClient();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        getFusedLocation();

    }

    @Override
    public void getNewStatus(int status) {

        switch (status) {

            case LocationProvider.OUT_OF_SERVICE:
                Log.e(TAG, "Location provider out of service");
                break;

            case LocationProvider.AVAILABLE:
                Log.e(TAG, "Location provider available");
                break;

        }

    }

    @Override
    public void getNewFusedLocation(final Location location) {

        if(location != null) {

            Log.e(TAG, location.toString());
            Log.e(TAG, "LATITUDE: " + String.valueOf(location.getLatitude()));
            Log.e(TAG, "LONGITUDE: " + String.valueOf(location.getLongitude()));
            Log.e(TAG, "PROVIDER: " + location.getProvider());

            if (fusedLocationProvider != null) {

                fusedLocationProvider.disconnect();

            }

            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
            mapController.setZoom(20);
            mapController.setCenter(geoPoint);

            Marker marker = new Marker(mapView);
            marker.setPosition(geoPoint);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle("Current Location");
            mapView.getOverlays().add(marker);

        }

    }

    private void checkGooglePlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {

            if (googleApiAvailability.isUserResolvableError(resultCode)) {

                googleApiAvailability.getErrorDialog(this, resultCode, 0).show();

            }

        } else {

            initializeGoogleApiClient();

        }

    }

    private void initializeGoogleApiClient() {

        GoogleApiClient.Builder googleApiClientBuilder = new GoogleApiClient.Builder(MapActivity.this);
        googleApiClientBuilder.addApi(LocationServices.API);
        googleApiClientBuilder.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

            @Override
            public void onConnected(@Nullable Bundle bundle) {



            }

            @Override
            public void onConnectionSuspended(int i) {



            }

        });
        googleApiClientBuilder.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {

            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {



            }

        });

        GoogleApiClient googleApiClient = googleApiClientBuilder.build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(60000);
        locationRequest.setSmallestDisplacement(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder();
        locationSettingsRequestBuilder.addLocationRequest(locationRequest);
        locationSettingsRequestBuilder.setAlwaysShow(true);

        LocationServices.getSettingsClient(this).checkLocationSettings(locationSettingsRequestBuilder.build()).addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {

            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {

                    task.getResult(ApiException.class);

                    getFusedLocation();

                } catch (ApiException exception) {

                    switch (exception.getStatusCode()) {

                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {

                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                resolvable.startResolutionForResult(MapActivity.this, 0);

                            } catch (IntentSender.SendIntentException e) {

                                e.printStackTrace();

                            } catch (ClassCastException e) {

                                e.printStackTrace();

                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;

                    }
                }

            }

        });

    }

    public void getFusedLocation() {

        if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {

            if (fusedLocationProvider != null) {

                fusedLocationProvider.disconnect();

            }

            fusedLocationProvider = new FusedLocationProvider(MapActivity.this, this);

            fusedLocationProvider.connect();

        } else {

            ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        }

    }

}
