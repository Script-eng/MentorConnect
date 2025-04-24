package hu.nje.mentorconnect.models;

public class Message {
    private String sender;
    private String text;
    private long timestamp;

    public Message() {
        // Required for Firestore
    }

    public Message(String sender, String text, long timestamp) {
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSender() { return sender; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }

    public void setSender(String sender) { this.sender = sender; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}