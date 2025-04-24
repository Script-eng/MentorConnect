package hu.nje.mentorconnect.models;

public class Mentor {
    private String name;
    private String major;
    private int imageResId;

    public Mentor(String name, String major, int imageResId) {
        this.name = name;
        this.major = major;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public String getMajor() { return major; }
    public int getImageResId() { return imageResId; }
}