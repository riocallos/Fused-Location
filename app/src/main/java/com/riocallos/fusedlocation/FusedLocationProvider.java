package com.riocallos.fusedlocation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import therapist.spanda.com.AppState;


/**
 * Created by racallos on 9/7/15.
 */
public class FusedLocationProvider implements LocationListener {

    private String TAG = FusedLocationProvider.class.getSimpleName();

    private Context context;
    
    private FusedLocationProviderClient fusedLocationProviderClient;
    
    private LocationRequest locationRequest;
    
    private LocationCallback locationCallback;
    
    private LocationResultCallback locationResultCallback;
    
    public abstract interface LocationResultCallback {

        //public void getNewStatus(int status);
        public void getNewFusedLocation(Location location);

    }

    public FusedLocationProvider(Context context, LocationResultCallback locationResultCallback) {

        this.context = context;
        this.locationResultCallback = locationResultCallback;
    
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient( this.context);

    }

    public void connect() {

        System.out.println("FusedLocationProvider.connect()");
        
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        
            locationRequest = new LocationRequest();
            locationRequest.setInterval(60000);
            locationRequest.setFastestInterval(60000);
            locationRequest.setSmallestDisplacement(0);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    
            locationCallback = new LocationCallback() {
    
                @Override
                public void onLocationResult(LocationResult locationResult) {
        
                    for(Location location : locationResult.getLocations()) {
            
                        System.out.println(location.toString());
                        
                        locationResultCallback.getNewFusedLocation(location);
                        
                    }
        
                }
    
            };
            
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,null);
            
        }
  
    }

    public void disconnect() {

       Log.e(TAG, "FusedLocationProvider.disconnect()");
    
        if(fusedLocationProviderClient != null) {
    
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    
        }

    }
    
    @Override
    public void onLocationChanged(Location location) {

        Log.e(TAG,"FusedLocationProvider.onLocationChanged()");

        if(location != null) {

            AppState.getInstance().setValue(AppState.KEY_LATITUDE, String.valueOf(location.getLatitude()));
            AppState.getInstance().setValue(AppState.KEY_LONGITUDE, String.valueOf(location.getLongitude()));

        }
        
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

        Log.e(TAG,"FusedLocationProvider.onStatusChanged()");

        //locationResultCallback.getNewStatus(status);


    }

    @Override
    public void onProviderEnabled(String provider) {

        Log.e(TAG,"FusedLocationProvider.onProviderEnabled()");

    }

    @Override
    public void onProviderDisabled(String provider) {

        Log.e(TAG,"FusedLocationProvider.onProviderDisabled()");

    }

}
