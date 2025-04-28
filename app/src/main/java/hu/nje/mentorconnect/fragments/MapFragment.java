package hu.nje.mentorconnect.fragments;

// Android & Support Library Imports
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

// Material Components Imports
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// osmdroid Imports
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider; // Needed for location potentially
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay; // Needed for location potentially

// Java Util Imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Project Specific Imports
import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.models.LocationInfo;


public class MapFragment extends Fragment {

    // Map and Controller
    private MapView mapView = null;
    private IMapController mapController = null;

    // UI Elements
    private FloatingActionButton fabToggleFavorites;

    // Data & State
    private SharedPreferences prefs;
    private Set<String> favoriteLocationIds;
    private List<LocationInfo> locations = new ArrayList<>();
    private boolean showingOnlyFavorites = false; // ** Filter state **

    // Resources
    private Drawable defaultMarkerIcon;
    private Drawable favoriteMarkerIcon;

    // Constants
    private static final String PREFS_NAME = "MapPrefs";
    private static final String KEY_FAVORITES = "favoriteLocations";
    private static final String KEY_FILTER_STATE = "mapFilterState"; // ** Key for saving filter state **
    private static final String TAG = "MapFragmentEnhanced";

    // Functional interface for the navigation callback
    @FunctionalInterface
    interface NavigationCallback {
        void onNavigate(LocationInfo location);
    }

    // --- Fragment Lifecycle Methods ---

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // Load SharedPreferences
        prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load favorite location IDs
        favoriteLocationIds = new HashSet<>(prefs.getStringSet(KEY_FAVORITES, new HashSet<>()));

        // ** Load the last filter state (default to showing all) **
        showingOnlyFavorites = prefs.getBoolean(KEY_FILTER_STATE, false);
        Log.d(TAG, "Loaded initial filter state - showingOnlyFavorites: " + showingOnlyFavorites);


        // Load marker icons
        loadMarkerIcons();

        // Define locations
        defineLocations();

        // Update favorite status based on loaded IDs
        updateInitialFavoriteStatus();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        // Optional: osmdroid Configuration (if not done globally)
        // ...

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize Views
        mapView = view.findViewById(R.id.map_view_osm);
        fabToggleFavorites = view.findViewById(R.id.fab_toggle_favorites);

        if (mapView == null || fabToggleFavorites == null) {
            Log.e(TAG, "Critical view (MapView or FAB) not found in layout!");
            return view;
        }

        // Setup Map Components
        setupMap(); // Includes enabling zoom controls now
        addLocationMarkers(); // Adds *all* defined markers initially
        zoomToFitMarkers(); // Zooms based on *initially visible* markers (respects loaded filter state)
        setupFab(); // Sets up the FAB listener and initial icon

        // Apply the loaded filter state visually AFTER markers are added
        // Note: addLocationMarkers now calls filterMarkers at the end, which uses the loaded 'showingOnlyFavorites' state.

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (mapView != null) {
            mapView.onResume();
        }
        // No need to reload state here unless it can change while paused & fragment is visible
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        // Save the current favorite state AND filter state persistently
        saveState();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        if (mapView != null) {
            InfoWindow.closeAllInfoWindowsOn(mapView);
            mapView.onDetach();
        }
        mapView = null;
        mapController = null;
        fabToggleFavorites = null;
        locations.clear();
        defaultMarkerIcon = null;
        favoriteMarkerIcon = null;
    }

    // --- Initialization and Setup ---

    private void loadMarkerIcons() {
        // ... (same as before) ...
        try {
            if (getContext() != null) {
                defaultMarkerIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker);
                favoriteMarkerIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker_favorite);
                if (defaultMarkerIcon == null || favoriteMarkerIcon == null) {
                    Log.w(TAG, "One or more marker icons failed to load.");
                }
            } else {
                Log.e(TAG,"Context is null during icon loading.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading marker drawables", e);
        }
    }

    private void defineLocations() {
        // ... (same as before, using actual coordinates) ...
        locations.clear();
        locations.add(new LocationInfo("DORM_ID", "Homokbanya Kollegium", new GeoPoint(46.88299, 19.64579), "Open 24/7 (Reception)"));
        locations.add(new LocationInfo("SCHOOL_ID", "Neumann Janos Egyetem", new GeoPoint(46.8960763, 19.66656), "Mon-Fri: 8:00 - 16:00"));
        locations.add(new LocationInfo("SHOPPING_ID", "Auchan Aruhaz", new GeoPoint(46.8877749, 19.6347568), "Mon-Sun: 7:00 - 21:00"));
        locations.add(new LocationInfo("STATION_ID", "Vasutallomas", new GeoPoint(46.91302, 19.697995), "See MAV schedule"));
        Log.d(TAG, "Defined " + locations.size() + " locations.");
    }

    private void updateInitialFavoriteStatus() {
        // ... (same as before) ...
        for (LocationInfo loc : locations) {
            loc.setFavorite(favoriteLocationIds.contains(loc.getId()));
        }
    }

    private void setupMap() {
        if (mapView == null || getContext() == null) {
            Log.e(TAG, "setupMap called with null MapView or Context");
            return;
        }
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true); // Enable pinch zoom

        // --- Enable Built-in Zoom Controls ---
        mapView.setBuiltInZoomControls(true);
        // Optional: Configure how they appear (e.g., fade out)
        mapView.getZoomController().setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        // --- End Zoom Controls ---


        mapController = mapView.getController();

        // --- Overlays ---
        // MapEventsOverlay: Closes InfoWindows on map tap. Add first (index 0).
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                InfoWindow.closeAllInfoWindowsOn(mapView);
                return false; // Let other overlays process the tap
            }
            @Override
            public boolean longPressHelper(GeoPoint p) { return false; }
        };
        mapView.getOverlays().add(0, new MapEventsOverlay(mapEventsReceiver));

        // Other Overlays (added after MapEventsOverlay)
        CompassOverlay compassOverlay = new CompassOverlay(getContext(), new InternalCompassOrientationProvider(getContext()), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(mapView);
        rotationGestureOverlay.setEnabled(true);
        mapView.getOverlays().add(rotationGestureOverlay);

        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        scaleBarOverlay.setCentred(true);
        scaleBarOverlay.setScaleBarOffset(getResources().getDisplayMetrics().widthPixels / 2, 15); // Adjust offset as needed
        mapView.getOverlays().add(scaleBarOverlay);
        // --- End Overlays ---
        Log.d(TAG, "Map setup complete with overlays and zoom controls.");
    }

    private void setupFab() {
        // Set initial icon based on the loaded filter state
        updateFabIcon();
        // Set click listener to toggle filter state
        fabToggleFavorites.setOnClickListener(v -> {
            showingOnlyFavorites = !showingOnlyFavorites; // Toggle the state
            Log.d(TAG, "FAB clicked. Showing only favorites: " + showingOnlyFavorites);
            // The FAB's purpose is to FILTER the markers currently displayed on the map.
            // It hides/shows markers based on their favorite status.
            filterMarkers(); // Apply the marker visibility filter
            updateFabIcon(); // Update the FAB icon to reflect the new state
            InfoWindow.closeAllInfoWindowsOn(mapView); // Close info window when filter changes
        });
        Log.d(TAG, "FAB setup complete.");
    }

    // --- Map Interaction ---

    private void addLocationMarkers() {
        if (mapView == null || locations.isEmpty() || getContext() == null) {
            Log.w(TAG, "addLocationMarkers skipped: MapView null, locations empty, or Context null.");
            return;
        }
        Log.d(TAG, "Adding " + locations.size() + " location markers...");

        // Clear existing markers before adding new ones to avoid duplicates if called multiple times
        // Although in current flow, it's only called once in onCreateView
        clearMarkers();

        for (LocationInfo location : locations) {
            Marker marker = new Marker(mapView);
            marker.setPosition(location.getGeoPoint());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(location.getName());
            marker.setSubDescription(location.getOfficeHours());
            marker.setSnippet(location.getOfficeHours());
            marker.setIcon(location.isFavorite() ? favoriteMarkerIcon : defaultMarkerIcon);
            marker.setRelatedObject(location);
            marker.setInfoWindow(new LocationInfoWindow(R.layout.info_window_custom, mapView, location, this::startNavigation));

            // Marker Click Listener
            marker.setOnMarkerClickListener((m, v) -> {
                Log.d(TAG, "Marker clicked: " + m.getTitle());
                Object related = m.getRelatedObject();
                if (related instanceof LocationInfo) {
                    toggleFavorite((LocationInfo) related, m);
                    InfoWindow.closeAllInfoWindowsOn(v); // Close others first
                    v.postDelayed(() -> { // Use postDelayed for robustness
                        if (m.getInfoWindow() != null && !m.isInfoWindowOpen()) {
                            m.showInfoWindow();
                            Log.d(TAG, "Showing info window for " + m.getTitle());
                        }
                    }, 50);
                } else {
                    Log.w(TAG, "Marker related object invalid on click.");
                }
                return true; // Event handled
            });

            mapView.getOverlays().add(marker);
        }

        // Apply the initial filter based on the loaded state AFTER adding all markers
        filterMarkers();

        mapView.invalidate(); // Redraw map
        Log.d(TAG, "Finished adding markers and applied initial filter.");
    }

    // Helper to remove only Marker overlays
    private void clearMarkers() {
        if (mapView == null) return;
        List<Overlay> overlaysToRemove = new ArrayList<>();
        for (Overlay overlay : mapView.getOverlays()) {
            if (overlay instanceof Marker) {
                // Close info window if it belongs to this marker
                ((Marker) overlay).closeInfoWindow();
                overlaysToRemove.add(overlay);
            }
        }
        mapView.getOverlays().removeAll(overlaysToRemove);
        Log.d(TAG,"Cleared existing markers.");
    }


    private void zoomToFitMarkers() {
        if (mapView == null || locations.isEmpty() || mapController == null) return;

        mapView.post(() -> { // Ensure layout is ready
            ArrayList<GeoPoint> pointsToShow = new ArrayList<>();
            // Collect points that are currently *visible* based on filter
            for (LocationInfo loc : locations) {
                // Check if the marker for this location is currently enabled
                boolean isVisible = !showingOnlyFavorites || loc.isFavorite();
                if(isVisible) {
                    pointsToShow.add(loc.getGeoPoint());
                }
            }

            if (pointsToShow.isEmpty()) {
                Log.w(TAG, "No visible markers to zoom to. Setting default view.");
                // No visible points (e.g., showing favorites only, but none are favorited)
                mapController.setZoom(12.0); // Zoom out to a default level
                mapController.setCenter(new GeoPoint(46.9, 19.66)); // Center on general Kecskemet area
            }
            else if (pointsToShow.size() == 1) {
                Log.d(TAG, "Zooming to single visible marker.");
                mapController.setZoom(15.0); // Zoom level for a single point
                mapController.setCenter(pointsToShow.get(0));
            } else {
                Log.d(TAG, "Calculating bounding box for " + pointsToShow.size() + " visible points.");
                try {
                    BoundingBox boundingBox = BoundingBox.fromGeoPoints(pointsToShow);
                    // Use zoomToBoundingBox for multiple points
                    mapView.zoomToBoundingBox(boundingBox, true, 150); // Animate, 150px padding
                    Log.d(TAG, "Zoomed to bounding box for visible markers.");
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error calculating bounding box (points might be identical?)", e);
                    mapController.setZoom(14.0); // Fallback zoom
                    mapController.setCenter(pointsToShow.get(0)); // Center on first visible point
                } catch (Exception e) { // Catch unexpected errors
                    Log.e(TAG, "Unexpected error zooming to bounding box", e);
                    mapController.setZoom(14.0);
                    mapController.setCenter(pointsToShow.get(0));
                }
            }
        });
    }

    // --- Favorite Handling ---

    private void toggleFavorite(LocationInfo location, Marker marker) {
        // ... (logic is same: toggle state, update icon, update favoriteLocationIds set, show toast) ...
        boolean isNowFavorite = !location.isFavorite();
        location.setFavorite(isNowFavorite);
        marker.setIcon(isNowFavorite ? favoriteMarkerIcon : defaultMarkerIcon);

        if (isNowFavorite) {
            favoriteLocationIds.add(location.getId());
            showToast(getString(R.string.favorite_added_toast, location.getName()));
            Log.i(TAG, location.getName() + " added to favorites.");
        } else {
            favoriteLocationIds.remove(location.getId());
            showToast(getString(R.string.favorite_removed_toast, location.getName()));
            Log.i(TAG, location.getName() + " removed from favorites.");
        }
        // Don't save prefs here, onPause does it.
        if (mapView != null) mapView.invalidate(); // Redraw marker icon
    }

    private void filterMarkers() {
        if (mapView == null) return;
        Log.d(TAG, "Applying marker filter. Showing only favorites: " + showingOnlyFavorites);
        boolean anyVisible = false;
        for (Overlay overlay : mapView.getOverlays()) {
            if (overlay instanceof Marker) {
                Marker marker = (Marker) overlay;
                Object relatedObject = marker.getRelatedObject();
                if (relatedObject instanceof LocationInfo) {
                    LocationInfo location = (LocationInfo) relatedObject;
                    // Determine if this marker should be visible based on the filter state
                    boolean shouldBeVisible = !showingOnlyFavorites || location.isFavorite();
                    // Enable/disable the marker (controls visibility and clickability)
                    marker.setEnabled(shouldBeVisible);
                    if (shouldBeVisible) {
                        anyVisible = true;
                    }
                }
            }
        }
        mapView.invalidate(); // Redraw map with updated visibility
        // Only re-zoom if the filter *might* have changed the visible set significantly
        // Avoid excessive zooming on every filter toggle if view is okay
        if (mapView.getZoomLevelDouble() < 5 || !anyVisible) { // Re-zoom if zoomed way out or nothing visible
            zoomToFitMarkers();
        }
        Log.d(TAG, "Marker filtering complete. Any visible: " + anyVisible);
    }

    private void updateFabIcon() {
        if (fabToggleFavorites == null) return;
        // Set the FAB icon based on the current filter state
        if (showingOnlyFavorites) {
            fabToggleFavorites.setImageResource(R.drawable.ic_star_filled);
        } else {
            fabToggleFavorites.setImageResource(R.drawable.ic_star_outline);
        }
    }

    // Save both favorites and filter state
    private void saveState() {
        if (prefs != null) {
            prefs.edit()
                    .putStringSet(KEY_FAVORITES, favoriteLocationIds)
                    .putBoolean(KEY_FILTER_STATE, showingOnlyFavorites) // Save filter state
                    .apply();
            Log.d(TAG, "Saved favorites and filter state to SharedPreferences.");
        }
    }

    // --- Navigation ---

    private void startNavigation(LocationInfo destination) {
        // ... (logic is same) ...
        if (getContext() == null || destination == null) {
            Log.w(TAG, "startNavigation skipped: Context or destination null.");
            return;
        }
        Log.i(TAG, "Starting navigation intent for: " + destination.getName());
        Uri gmmIntentUri = Uri.parse(String.format(java.util.Locale.US, "geo:%f,%f?q=%f,%f(%s)",
                destination.getGeoPoint().getLatitude(), destination.getGeoPoint().getLongitude(),
                destination.getGeoPoint().getLatitude(), destination.getGeoPoint().getLongitude(),
                Uri.encode(destination.getName())
        ));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        try {
            if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                showToast(getString(R.string.no_navigation_app));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting navigation intent", e);
            showToast(getString(R.string.no_navigation_app));
        }
    }

    // --- Utility Methods ---

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }


    // --- Custom InfoWindow Inner Class ---
    // No changes needed here from the previous version
    private static class LocationInfoWindow extends InfoWindow {
        private final LocationInfo currentLocation;
        private final NavigationCallback navigationCallback;

        LocationInfoWindow(int layoutResId, MapView mapView, LocationInfo location, NavigationCallback callback) {
            super(layoutResId, mapView);
            this.currentLocation = location;
            this.navigationCallback = callback;
        }

        @Override
        public void onOpen(Object item) {
            Button btnNavigate = mView.findViewById(R.id.info_navigate_button);
            TextView infoTitle = mView.findViewById(R.id.info_title);
            TextView infoSnippet = mView.findViewById(R.id.info_snippet);

            if (infoTitle != null) {
                infoTitle.setText(currentLocation != null ? currentLocation.getName() : "Error");
            }
            if (infoSnippet != null) {
                infoSnippet.setText(currentLocation != null ? currentLocation.getOfficeHours() : "");
            }
            if (btnNavigate != null) {
                btnNavigate.setOnClickListener(v -> {
                    if (navigationCallback != null && currentLocation != null) {
                        navigationCallback.onNavigate(currentLocation);
                    }
                    close();
                });
            }
            Log.d(TAG, "Info window opened for: " + (currentLocation != null ? currentLocation.getName() : "unknown"));
        }

        @Override
        public void onClose() {
            Log.d(TAG, "Info window closed for: " + (currentLocation != null ? currentLocation.getName() : "unknown"));
        }
    } // --- End InfoWindow Inner Class ---

} // --- End MapFragment Class ---