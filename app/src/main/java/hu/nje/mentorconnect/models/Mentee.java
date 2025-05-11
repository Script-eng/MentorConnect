package hu.nje.mentorconnect.models;

public class Mentee {
    private String name;
    private String email;
    private String assignedMentorId;

    public Mentee() {} // Required for Firestore

    public Mentee(String name, String email, String assignedMentorId) {
        this.name = name;
        this.email = email;
        this.assignedMentorId = assignedMentorId;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAssignedMentorId() { return assignedMentorId; }
}