package hu.nje.mentorconnect.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat; // For getting drawables
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint; // Use osmdroid's GeoPoint
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay; // For rotation
import org.osmdroid.views.overlay.ScaleBarOverlay; // For scale bar

import java.util.ArrayList;

import hu.nje.mentorconnect.R;

public class MapFragment extends Fragment {

    private MapView mapView = null;
    private IMapController mapController = null;
    private static final String TAG = "MapFragmentOsm";

    // --- Coordinates - Use osmdroid GeoPoint --- 46.8829899,19.64091
    private static final GeoPoint COORD_SCHOOL = new GeoPoint(46.8965, 19.6661); // Example: Budapest Center (Replace!)
    private static final GeoPoint COORD_MARKET = new GeoPoint(47.4979, 19.0402); // Example: Near Parliament (Replace!)
    private static final GeoPoint COORD_DORM = new GeoPoint(46.8829, 19.6409);   // Example: Near Nyugati (Replace!)
    // --- End Coordinates ---

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.map_view_osm);
        if (mapView == null) {
            Log.e(TAG, "MapView not found in layout!");
            return view; // Or handle error appropriately
        }

        setupMap();

        return view;
    }

    private void setupMap() {
        if (mapView == null) return;

        Log.d(TAG, "Setting up osmdroid map.");

        // Set the tile source (DEFAULT_TILE_SOURCE uses OpenStreetMap.org's standard tiles)
        mapView.setTileSource(TileSourceFactory.MAPNIK); // MAPNIK is the standard OSM style

        // Enable Multi-Touch Controls (Zooming)
        mapView.setMultiTouchControls(true);

        // Enable Rotation Gestures (optional)
        RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(mapView);
        rotationGestureOverlay.setEnabled(true);
        mapView.getOverlays().add(rotationGestureOverlay);

        // Add Compass Overlay (optional)
        CompassOverlay compassOverlay = new CompassOverlay(getContext(), new InternalCompassOrientationProvider(getContext()), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        // Add Scale Bar Overlay (optional)
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        scaleBarOverlay.setCentred(true);
        // Play around with these values to find the best placement:
        scaleBarOverlay.setScaleBarOffset(getResources().getDisplayMetrics().widthPixels / 2, 10);
        mapView.getOverlays().add(scaleBarOverlay);


        // --- Get Map Controller and Set Initial View ---
        mapController = mapView.getController();
        mapController.setZoom(14.0); // Set initial zoom level (double)
        mapController.setCenter(COORD_SCHOOL); // Center on the school initially
        // --- End Initial View ---


        // --- Add Markers ---
        Log.d(TAG, "Adding markers.");
        addMarker(COORD_SCHOOL, "Neumann Janos Egyetem");
        addMarker(COORD_MARKET, "Market Place");
        addMarker(COORD_DORM, "Dormitory");
        // --- End Markers ---

        // Refresh the map to draw overlays
        mapView.invalidate();
        Log.i(TAG, "Map setup complete.");
    }

    private void addMarker(GeoPoint position, String title) {
        if (mapView == null || getContext() == null) return;

        Marker marker = new Marker(mapView);
        marker.setPosition(position);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); // Position the icon anchor
        marker.setTitle(title); // Text shown when tapped (in an info window)

        // Optional: Set a custom icon (otherwise uses default marker)
        try {
            // Make sure you have an 'ic_marker.png' or similar in your drawable folders
            Drawable markerIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker); // Replace with your marker icon
            if (markerIcon != null) {
                marker.setIcon(markerIcon);
            } else {
                Log.w(TAG, "Marker icon drawable not found.");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting marker icon", e);
        }


        // Add an InfoWindow (optional - basic example)
        // You can create custom info window layouts
        /* Basic Info Window setup
        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                marker.showInfoWindow(); // Shows the title set above
                mapView.getController().animateTo(marker.getPosition()); // Center on marker
                return true; // Consume event
            }
        });
        */

        mapView.getOverlays().add(marker);
        Log.d(TAG, "Added marker for: " + title);
    }


    // --- osmdroid Lifecycle Handling ---
    @Override
    public void onResume() {
        super.onResume();
        // Needed for compass, map rendering, etc.
        if (mapView != null) {
            mapView.onResume();
            Log.d(TAG, "MapView onResume");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Needed for compass, map rendering, etc.
        if (mapView != null) {
            mapView.onPause();
            Log.d(TAG, "MapView onPause");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up map view to prevent memory leaks
        if (mapView != null) {
            mapView.onDetach();
            Log.d(TAG, "MapView onDetach");
        }
        mapView = null;
        mapController = null;
        Log.d(TAG, "MapFragmentOsm onDestroyView");
    }
}