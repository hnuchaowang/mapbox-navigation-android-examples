package com.mapbox.navigation.examples.standalone.location;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.common.location.Location;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin;
import com.mapbox.maps.plugin.animation.CameraAnimationsUtils;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp;
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver;
import com.mapbox.navigation.core.lifecycle.RequireMapboxNavigationDelegate;
import com.mapbox.navigation.core.trip.session.LocationMatcherResult;
import com.mapbox.navigation.core.trip.session.LocationObserver;
import com.mapbox.navigation.examples.databinding.MapboxActivityUserCurrentLocationBinding;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;

import kotlin.properties.ReadOnlyProperty;

public class ShowCurrentLocationActivity extends AppCompatActivity {

    private NavigationLocationProvider navigationLocationProvider = new NavigationLocationProvider();

    private LocationObserver locationObserver = new LocationObserver() {
        @Override
        public void onNewLocationMatcherResult(@NonNull LocationMatcherResult locationMatcherResult) {
            navigationLocationProvider.changePosition(locationMatcherResult.getEnhancedLocation(), locationMatcherResult.getKeyPoints(), null, null);
            updateCamera(locationMatcherResult.getEnhancedLocation());
        }

        @Override
        public void onNewRawLocation(@NonNull com.mapbox.common.location.Location location) {

        }
    };

    private MapboxActivityUserCurrentLocationBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MapboxActivityUserCurrentLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.mapView.getMapboxMap().loadStyle(Style.STANDARD);
    }

    @SuppressLint("MissingPermission")
    ReadOnlyProperty<Object, MapboxNavigation> delegate = new RequireMapboxNavigationDelegate(
            this,
            null,
            null,
            new MapboxNavigationObserver() {
                @Override
                public void onAttached(@NonNull MapboxNavigation mapboxNavigation) {
                    mapboxNavigation.registerLocationObserver(locationObserver);
                    mapboxNavigation.startTripSession();
                }

                @Override
                public void onDetached(@NonNull MapboxNavigation mapboxNavigation) {
                    mapboxNavigation.unregisterLocationObserver(locationObserver);
                }
            },
            () -> {
                ShowCurrentLocationActivity.this.initNavigation();
                return null;
            }
    );

    private void initNavigation() {
        Log.d("chawan,", "init");
        MapboxNavigationApp.setup(new NavigationOptions.Builder(this).build());
        LocationComponentPlugin plugin = LocationComponentUtils.getLocationComponent(binding.mapView);
        plugin.setLocationProvider(navigationLocationProvider);
        plugin.setEnabled(true);
    }


    private void updateCamera(Location location) {
        MapAnimationOptions mapAnimationOptions = (new MapAnimationOptions.Builder()).duration(1500L).build();
        CameraAnimationsPlugin cameraAnimationsPlugin = CameraAnimationsUtils.getCamera(binding.mapView);
        CameraOptions cameraOptions = new CameraOptions.Builder().center(Point.fromLngLat(location.getLongitude(), location.getLatitude())).zoom(12.0).padding(new EdgeInsets(500.0, 0.0, 0.0, 0.0)).build();
        cameraAnimationsPlugin.easeTo(cameraOptions, mapAnimationOptions, null);
    }
}
