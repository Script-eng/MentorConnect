package hu.nje.mentorconnect.models;

public class Mentor {
    private String name;
    private String department;
    private String email;
    private String uid;

    public Mentor() {}

    public Mentor(String name, String department, String email, String uid) {
        this.name = name;
        this.department = department;
        this.email = email;
        this.uid = uid;
    }

    // Getters
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getEmail() { return email; }
    public String getUid() { return uid; }
}