package data;

import com.google.firebase.firestore.PropertyName;

public class Crime {

    // 1. Map Firestore "CrimeID" to Java "crime_id"
    @PropertyName("CrimeID")
    public String crime_id;

    // 2. Map Firestore "Month" to Java "month"
    @PropertyName("Month")
    public String month;

    // 3. Map Firestore "ReportedBy"
    @PropertyName("ReportedBy")
    public String reported_by;

    // 4. Map Firestore "FallsWithin"
    @PropertyName("FallsWithin")
    public String falls_within;

    // 5. Map Firestore "Latitude"
    @PropertyName("Latitude")
    public Double latitude;

    // 6. Map Firestore "Longitude"
    @PropertyName("Longitude")
    public Double longitude;

    // 7. Map Firestore "CrimeType"
    @PropertyName("CrimeType")
    public String crime_type;

    // 8. Map Firestore "Outcome" to Java "last_outcome"
    @PropertyName("Outcome")
    public String last_outcome;

    // These fields presumably don't need mapping or aren't in the logs
    public String location_desc;
    public String lsoa_code;
    public String lsoa_name;
    public String context;
    public Double location;

    // Required empty constructor
    public Crime() {}

    // --- Helper Methods (Keep these for your App logic) ---

    // Used by DocumentSnapshot to set ID manually if needed
    public void setId(String id) {
        this.crime_id = id;
    }

    // Explicit Getters/Setters (Optional if fields are public, but good for safety)

    @PropertyName("Longitude")
    public Double getLongitude() {
        return longitude;
    }

    @PropertyName("Longitude")
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @PropertyName("Latitude")
    public Double getLatitude() {
        return latitude;
    }

    @PropertyName("Latitude")
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getLocationDesc() {
        return location_desc;
    }

    public void setLocationDesc(String location_desc) {
        this.location_desc = location_desc;
    }

    @PropertyName("CrimeType")
    public String getCrimeType() {
        return crime_type;
    }

    @PropertyName("CrimeType")
    public void setCrimeType(String crime_type) {
        this.crime_type = crime_type;
    }

    // Helper for "Outcome" if you access it via getOutcome() in your code
    @PropertyName("Outcome")
    public String getOutcome() {
        return last_outcome;
    }

    public Double getLocation() {
        return location;
    }

    public void setLocation(Double location) {
        this.location = location;
    }

    public String getTitle() {
        return crime_type != null ? crime_type : "";
    }
}
