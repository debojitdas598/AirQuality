package com.example.airquality;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;

public class UserLocation {
    private Context context;
    private LocationManager locationManager;

    public UserLocation(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public double getLatitude() {
        Location location = getLocation();
        if (location != null) {
            return location.getLatitude();
        } else {
            return 0.0;
        }
    }

    public double getLongitude() {
        Location location = getLocation();
        if (location != null) {
            return location.getLongitude();
        } else {
            return 0.0;
        }
    }

    private Location getLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }
}
