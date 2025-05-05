package hu.nje.mentorconnect.models;

import com.google.firebase.Timestamp; // Import Firestore Timestamp

public class Conversation {
    private String chatRoomId;
    private String partnerId;       // UID of the other person in the chat
    private String partnerName;     // Display name of the other person
    private String lastMessageText;
    private Timestamp lastMessageTimestamp;
    // Optional: Add partnerImageUrl if you plan to show profile pictures

    public Conversation() {
        // Firestore constructor
    }

    // --- Getters ---
    public String getChatRoomId() { return chatRoomId; }
    public String getPartnerId() { return partnerId; }
    public String getPartnerName() { return partnerName; }
    public String getLastMessageText() { return lastMessageText; }
    public Timestamp getLastMessageTimestamp() { return lastMessageTimestamp; }

    // --- Setters ---
    public void setChatRoomId(String chatRoomId) { this.chatRoomId = chatRoomId; }
    public void setPartnerId(String partnerId) { this.partnerId = partnerId; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }
    public void setLastMessageText(String lastMessageText) { this.lastMessageText = lastMessageText; }
    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }

    // Optional: Method to get timestamp as long if needed for sorting/comparison
    public long getLastMessageTimestampMillis() {
        return lastMessageTimestamp != null ? lastMessageTimestamp.toDate().getTime() : 0;
    }
}