package hu.nje.mentorconnect.models;

import com.google.firebase.Timestamp; // Import Firestore Timestamp
import com.google.firebase.firestore.ServerTimestamp; // Import ServerTimestamp annotation

public class Message {
    private String senderId;       // Firebase UID of the sender
    private String recipientId;    // Firebase UID of the recipient
    private String text;
    private @ServerTimestamp Timestamp timestamp; // Use Firestore Timestamp, set by server

    public Message() {
        // Required empty public constructor for Firestore deserialization
    }

    // Constructor used when SENDING a message (timestamp is set by server)
    public Message(String senderId, String recipientId, String text) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.text = text;
        // timestamp field is left null here, Firestore fills it via @ServerTimestamp
    }

    // --- Getters ---
    public String getSenderId() { return senderId; }
    public String getRecipientId() { return recipientId; }
    public String getText() { return text; }
    public Timestamp getTimestamp() { return timestamp; } // Getter returns Timestamp

    // --- Setters (Needed by Firestore, but usually not called directly for timestamp) ---
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; } // Setter takes Timestamp

    // Optional: Method to get timestamp as long if needed for sorting/comparison
    public long getTimestampMillis() {
        return timestamp != null ? timestamp.toDate().getTime() : 0;
    }
}