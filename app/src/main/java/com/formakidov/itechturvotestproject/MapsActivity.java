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
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    @BindView(R.id.bottom_sheet)
    FrameLayout bottomSheetView;
    @BindView(R.id.coordinatorlayout)
    CoordinatorLayout coordinatorlayout;
    @BindView(R.id.background_view)
    View backgroundView;
    @BindView(R.id.bottom_sheet_toolbar)
    View bsToolbar;
    @BindView(R.id.bs_content_view)
    BottomSheetRecyclerView bsRecyclerView;

    private GoogleMap map;
    private BottomSheetBehavior<View> behaviour;

    private LocationController locationController;
    private Subscription locationPermissionSubscription;
    private LatLng currentLocation;

    public static final float INITIAL_BOTTOM_SHEET_SCALE = 0.9f;

    private CardsAdapter cardsAdapter;

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

        bsRecyclerView.setScaleX(INITIAL_BOTTOM_SHEET_SCALE);

        cardsAdapter = new CardsAdapter(this);
        List<CardData> items = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            items.add(new CardData("Item title #" + i, "Item subtitle #" + i));
        }
        cardsAdapter.setItems(items);
        bsRecyclerView.setAdapter(cardsAdapter);

        bsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        behaviour = BottomSheetBehavior.from(bottomSheetView);
        behaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            private static final float colorThreshold = 0.6f;
            private static final float toolbarThreshold = colorThreshold;

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                UiSettings mapSettings = map.getUiSettings();
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    Log.d("logf", "onStateChanged: STATE_COLLAPSED");
                    bsRecyclerView.setInterceptTouchEvents(true);
                    mapSettings.setScrollGesturesEnabled(true);
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    Log.d("logf", "onStateChanged: STATE_EXPANDED");
                    bsRecyclerView.setInterceptTouchEvents(false);
                    mapSettings.setScrollGesturesEnabled(false);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                UIUtils.hideKeyboard(MapsActivity.this); // TODO: 7/13/17
                float scaleRatio = INITIAL_BOTTOM_SHEET_SCALE + ((1 - INITIAL_BOTTOM_SHEET_SCALE) * (1 - (1 - slideOffset)));
                bsRecyclerView.setScaleX(scaleRatio);

                if (slideOffset < colorThreshold) {
                    backgroundView.setAlpha(slideOffset * (1f / colorThreshold));
                } else {
                    backgroundView.setAlpha(1);
                }
                if (slideOffset < toolbarThreshold) {
                    bsToolbar.setTranslationY(-toolbarTranslationLength);
                } else {
                    float translation = -(toolbarTranslationLength * ((1 - slideOffset) / (1 - colorThreshold)));
                    bsToolbar.setTranslationY(translation);
                }
            }
        });
        bsRecyclerView.setInterceptTouchEvents(true);

        behaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);

        findLocation();
    }

    @OnClick(R.id.btn_close)
    void onCloseBottomSheetClick() {
        UIUtils.hideKeyboard(MapsActivity.this);
        bsRecyclerView.scrollToPosition(0);
        behaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
        float curZoom = map.getCameraPosition().zoom;
        int defZoom = 14;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, curZoom > defZoom ? curZoom : defZoom);
        if (animated) {
            map.animateCamera(cameraUpdate);
        } else {
            map.moveCamera(cameraUpdate);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationController.stopUpdates();
        if (locationPermissionSubscription != null) {
            locationPermissionSubscription.unsubscribe();
        }
    }
}
