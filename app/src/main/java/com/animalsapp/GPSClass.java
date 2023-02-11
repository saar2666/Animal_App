package com.animalsapp;


import static android.content.Context.LOCATION_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GPSClass implements LocationListener {

    Context context;
    static Location location;
    LocationManager locationManager;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean isLocationEnabled = false;
    static double latitude = 0.0;
    static double longitude = 0.0;

    public GPSClass(Context context) {
        this.context = context;
        getLocation();
    }


    boolean loc=false;
    @SuppressLint("MissingPermission")
    public Location getLocation() {
        try {
            // check if gps enabled , else ask permission
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isNetworkEnabled && !isGPSEnabled) {

            }
            else {
                this.isLocationEnabled = true;

                if (isNetworkEnabled) {

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                        }
                    }
                }

                if (isGPSEnabled) {
                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
                    fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            Log.e("locationCHeck", "success");
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.e("locationCHeck", "" + latitude + ", " + longitude);
                            }
                            else {
                                Log.e("locationCHeck", "null");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("locationCHeck", "failure" + e.getLocalizedMessage());
                        }
                    });
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return location;

    }

    public double getLatitude()
    {
        if (location!=null){
            latitude=location.getLatitude();
        }
        return latitude;

    }

    public double getLongitude()
    {
        if (location!=null){
            longitude=location.getLongitude();
        }
        return longitude;
    }

    public boolean isLocationEnabled()
    {
        return this.isLocationEnabled;
    }





    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

}

