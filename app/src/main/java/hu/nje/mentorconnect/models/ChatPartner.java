package hu.nje.mentorconnect.models;

// Simple model to hold information about a user to display in the initial chat list
public class ChatPartner {
    private String userId;
    private String userName;
    // Optional: Add role, profile image URL if needed for the list display

    public ChatPartner() { }

    public ChatPartner(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
}