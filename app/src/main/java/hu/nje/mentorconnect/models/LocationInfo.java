package hu.nje.mentorconnect.models;

import org.osmdroid.util.GeoPoint;

public class LocationInfo {
    private String id; // Unique identifier (can be same as name if names are unique)
    private String name;
    private GeoPoint geoPoint;
    private String officeHours;
    private boolean isFavorite; // Transient state, loaded from SharedPreferences

    public LocationInfo(String id, String name, GeoPoint geoPoint, String officeHours) {
        this.id = id;
        this.name = name;
        this.geoPoint = geoPoint;
        this.officeHours = officeHours;
        this.isFavorite = false; // Default to not favorite
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public GeoPoint getGeoPoint() { return geoPoint; }
    public String getOfficeHours() { return officeHours; }
    public boolean isFavorite() { return isFavorite; }

    // Setter for favorite status
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    // Optional: Override equals and hashCode if needed for Set operations based on ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationInfo that = (LocationInfo) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}