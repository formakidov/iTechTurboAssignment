package com.formakidov.itechturvotestproject;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.tbruyelle.rxpermissions.RxPermissions;

import rx.Observable;


public class LocationController implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, com.google.android.gms.location.LocationListener {

    private static final String TAG = LocationController.class.getSimpleName();

    private LocationListener listener;
    private Activity activity;

    private GoogleApiClient client;
    private boolean shouldRequestLocationUpdates;
    private boolean isResumed;
    private Location lastKnownLocation;
    private boolean resolvingError;

    public boolean isLocationPermissionGranted() {
        RxPermissions rxPerm = RxPermissions.getInstance(activity);
        return rxPerm.isGranted(Manifest.permission.ACCESS_FINE_LOCATION) &&
                rxPerm.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public Observable<Boolean> grantLocationPermission() {
        return RxPermissions.getInstance(activity)
                .request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .onErrorReturn(e -> false);
    }

    public Observable<Boolean> shouldRequestRationale() {
        return RxPermissions.getInstance(activity)
                .shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .onErrorReturn(e -> false);
    }

    public LocationController(Activity activity, boolean shouldRequestLocationUpdates) {
        this.activity = activity;
        this.shouldRequestLocationUpdates = shouldRequestLocationUpdates;

        client = new GoogleApiClient.Builder(activity.getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public LocationController(Activity activity) {
        this(activity, false);
    }

    public void setLocationListener(LocationListener listener) {
        this.listener = listener;
    }

    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    public void resumeUpdates() {
        isResumed = true;
        if (!client.isConnected()) {
            Log.d(TAG, "resumeUpdates: connecting");
            client.connect();
        } else {
            Log.d(TAG, "resumeUpdates: starting location updates");
            startLocationUpdates();
        }
    }

    public void pauseUpdates() {
        Log.d(TAG, "pauseUpdates: disconnecting (stopping loc updates)");
        isResumed = false;
        stopLocationUpdates();
        client.disconnect();
    }

    public void stopUpdates() {
        isResumed = false;
        stopLocationUpdates();
        client.disconnect();
        listener = null;
        activity = null;
    }

    private void startLocationUpdates() {
        if (client.isConnected()) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setFastestInterval(10000);
            locationRequest.setInterval(10000);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            checkSettings(locationRequest);
        }
    }

    private void requestLocationUpdates(LocationRequest locationRequest) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        } catch (SecurityException ignored) {}
    }

    private void checkSettings(LocationRequest locationRequest) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(client, builder.build());

        result.setResultCallback(locationSettingsResult -> {
            final Status status = locationSettingsResult.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    requestLocationUpdates(locationRequest);
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        status.startResolutionForResult(activity,
                                9001);
                    } catch (IntentSender.SendIntentException ignored) {}
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    listener.onLocationChangeUnavailable();
                    break;
            }
        });
    }

    private void stopLocationUpdates() {
        if (client.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (shouldRequestLocationUpdates && isResumed) {
            Log.d(TAG, "onConnected: stating location updates");
            startLocationUpdates();
        } else {
            Log.d(TAG, "onConnected: without updates");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed: ");
        if (resolvingError) {
            return;
        }
        if (result.hasResolution()) {
            try {
                resolvingError = true;
                result.startResolutionForResult(activity,
                        9000);
            } catch (IntentSender.SendIntentException e) {
                client.connect();
            }
        } else {
            resolvingError = true;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), activity,
                    9000);
            dialog.setOnDismissListener(dialogInterface -> resolvingError = false);
            dialog.show();
        }
    }

    public void setShouldRequestLocationUpdates(boolean shouldRequestLocationUpdates) {
        this.shouldRequestLocationUpdates = shouldRequestLocationUpdates;
    }

    @Override
    public void onLocationChanged(Location location) {
        lastKnownLocation = location;
        if (listener != null) {
            listener.onLocationChanged(location.getLatitude(), location.getLongitude());
        }
    }

    public interface LocationListener {
        void onLocationChanged(double latitude, double longitude);
        void onLocationChangeUnavailable();
    }
}

