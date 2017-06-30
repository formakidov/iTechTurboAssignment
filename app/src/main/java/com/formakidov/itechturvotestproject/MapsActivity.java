package com.formakidov.itechturvotestproject;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private BottomSheetBehavior<View> behaviour;

    @BindView(R.id.bottom_sheet)
    BottomSheetView bottomSheetView;
    @BindView(R.id.background_view)
    View backgroundView;
    @BindView(R.id.bottom_sheet_toolbar)
    View bsToolbar;
    @BindView(R.id.bs_content_view)
    TurvoLendingWebView bsContentView;

    private LocationController locationController;
    private Subscription locationPermissionSubscription;
    private LatLng currentLocation;
    private MarkerOptions currentLocationMarker;

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

        bsContentView.setScaleX(INITIAL_BOTTOM_SHEET_SCALE);

        findViewById(R.id.btn_close).setOnClickListener(v -> behaviour.setState(BottomSheetBehavior.STATE_COLLAPSED));

        behaviour = BottomSheetBehavior.from(bottomSheetView);
        behaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            private static final float colorThreshold = 0.6f;
            private static final float toolbarThreshold = colorThreshold;
//            private static final float scaleThreshold = 0.4f;

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bsContentView.setGesturesEnabled(false);
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    bsContentView.setGesturesEnabled(true);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                if (slideOffset < scaleThreshold) {
//                    if (bsContentView.getScaleX() != INITIAL_BOTTOM_SHEET_SCALE) {
//                        bsContentView.setScaleX(INITIAL_BOTTOM_SHEET_SCALE);
//                    }
//                } else {
                bsContentView.setScaleX(INITIAL_BOTTOM_SHEET_SCALE + (
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
                            }
                        });
                        locationController.resumeUpdates();
                    }
                });
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
        if (locationPermissionSubscription != null) {
            locationPermissionSubscription.unsubscribe();
        }
    }
}
