package hu.nje.mentorconnect.models;

import com.google.firebase.Timestamp;

public class Meeting {
    private String id;             // Firestore document ID (optional)
    private String title;
    private String location;
    private String purpose;
    private Timestamp date;
    private String mentorId;

    public Meeting() {} // Required for Firestore

    public Meeting(String title, String location, String purpose, Timestamp date, String mentorId) {
        this.title = title;
        this.location = location;
        this.purpose = purpose;
        this.date = date;
        this.mentorId = mentorId;
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public String getPurpose() { return purpose; }
    public Timestamp getDate() { return date; }
    public String getMentorId() { return mentorId; }

    // --- Setters ---
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setLocation(String location) { this.location = location; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public void setDate(Timestamp date) { this.date = date; }
    public void setMentorId(String mentorId) { this.mentorId = mentorId; }
}