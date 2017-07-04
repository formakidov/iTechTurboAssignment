package com.formakidov.itechturvotestproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    @BindView(R.id.bottom_sheet)
    BottomSheetView bottomSheetView;
    @BindView(R.id.coordinatorlayout)
    CoordinatorLayout coordinatorlayout;
    @BindView(R.id.background_view)
    View backgroundView;
    @BindView(R.id.bottom_sheet_toolbar)
    View bsToolbar;
    @BindView(R.id.bs_content_view)
    TurvoLendingWebView bsWebView;
    @BindView(R.id.bs_curtain_view)
    BottomSheetCurtainView bsCurtainView;

    private GoogleMap map;
    private BottomSheetBehavior<View> behaviour;

    private LocationController locationController;
    private Subscription locationPermissionSubscription;
    private LatLng currentLocation;

    public static final float INITIAL_BOTTOM_SHEET_SCALE = 0.9f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationController = new LocationController(this);

        final int toolbarHeight = getResources().getDimensionPixelOffset(R.dimen.bottom_sheet_toolbar_height);
        final int toolbarTranslationLength = getResources().getDimensionPixelOffset(R.dimen.bottom_sheet_toolbar_top_margin) + toolbarHeight;

        bsWebView.setScaleX(INITIAL_BOTTOM_SHEET_SCALE);

        findViewById(R.id.btn_close).setOnClickListener(v -> behaviour.setState(BottomSheetBehavior.STATE_COLLAPSED));

        behaviour = BottomSheetBehavior.from(bottomSheetView);
        behaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            private static final float colorThreshold = 0.6f;
            private static final float toolbarThreshold = colorThreshold;
//            private static final float scaleThreshold = 0.4f;

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    Log.d("logf", "onStateChanged: STATE_COLLAPSED");
                    bsWebView.setInterceptTouchEvents(true);
                    bsCurtainView.setInterceptTouchEvents(false);
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    Log.d("logf", "onStateChanged: STATE_EXPANDED");
                    bsWebView.setInterceptTouchEvents(false);
                    bsCurtainView.setInterceptTouchEvents(true);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                if (slideOffset < scaleThreshold) {
//                    if (bsWebView.getScaleX() != INITIAL_BOTTOM_SHEET_SCALE) {
//                        bsWebView.setScaleX(INITIAL_BOTTOM_SHEET_SCALE);
//                    }
//                } else {
                bsWebView.setScaleX(INITIAL_BOTTOM_SHEET_SCALE + (
                        (1 - INITIAL_BOTTOM_SHEET_SCALE) * (1 - (1 - slideOffset)/* / (1 - scaleThreshold)*/))
                );
//                }
                if (slideOffset < colorThreshold) {
                    backgroundView.setAlpha(slideOffset * (1f / colorThreshold));
                } else {
                    if (backgroundView.getAlpha() != 1) {
                        backgroundView.setAlpha(1);
                    }
                }
                if (slideOffset < toolbarThreshold) {
                    if (bsToolbar.getTranslationY() != -toolbarTranslationLength) {
                        bsToolbar.setTranslationY(-toolbarTranslationLength);
                    }
                } else {
                    float translation = -(toolbarTranslationLength * ((1 - slideOffset) / (1 - colorThreshold)));
                    bsToolbar.setTranslationY(translation);
                }
            }
        });
        bsCurtainView.setInterceptTouchEvents(false);
        bsWebView.setInterceptTouchEvents(true);

        behaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);

        findLocation();
    }

    private void findLocation() {
        locationPermissionSubscription = locationController.grantLocationPermission()
                .subscribe(granted -> {
                    if (granted) {
                        locationController.setShouldRequestLocationUpdates(true);
                        locationController.setLocationListener(new LocationController.LocationListener() {
                            @Override
                            public void onLocationChanged(double latitude, double longitude) {
                                setCurrentLocation(latitude, longitude);
                                zoomToCurrentLocation(true);
                            }

                            @Override
                            public void onLocationChangeUnavailable() {
                                onLocationUnavailable();
                            }
                        });
                        locationController.resumeUpdates();
                    } else {
                        locationController.shouldRequestRationale()
                                .subscribe(shouldRequestRationale -> {
                                    if (!shouldRequestRationale) {
                                        onLocationPermissionUnavailable();
                                    }
                                });
                    }
                });
    }

    public void onLocationUnavailable() {
        // Means user checked 'never ask again' for location service (gps)
        Snackbar.make(coordinatorlayout, R.string.gps_is_disabled, Snackbar.LENGTH_LONG)
                .setAction(R.string.settings, v -> {
                    final Intent i = new Intent();
                    i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(i);
                }).show();
    }

    public void onLocationPermissionUnavailable() {
        // Means user checked 'never ask again' for location permission
        Snackbar.make(coordinatorlayout, R.string.location_permission_is_denied, Snackbar.LENGTH_LONG)
                .setAction(R.string.settings, v -> {
                    final Intent i = new Intent();
                    i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    i.addCategory(Intent.CATEGORY_DEFAULT);
                    i.setData(Uri.parse("package:" + getPackageName()));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(i);
                }).show();
    }

    public void setCurrentLocation(double latitude, double longitude) {
        if (map == null) return;
        //noinspection MissingPermission
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        currentLocation = new LatLng(latitude, longitude);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(53.9, 27.5667), 5));
    }

    public void zoomToCurrentLocation(boolean animated) {
        if (map == null || currentLocation == null) return;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, 14);
        if (animated) {
            map.animateCamera(cameraUpdate);
        } else {
            map.moveCamera(cameraUpdate);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationController.stopUpdates();
        if (locationPermissionSubscription != null) {
            locationPermissionSubscription.unsubscribe();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        findLocation();
    }

    @Override
    protected void onStop() {
        locationController.pauseUpdates();
        super.onStop();
    }
}
