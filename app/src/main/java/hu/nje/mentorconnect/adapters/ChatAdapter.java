package hu.nje.mentorconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp; // Import Timestamp

import java.text.SimpleDateFormat; // For formatting timestamp
import java.util.List;
import java.util.Locale; // For locale in date format

import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.models.Message; // Import updated Message model

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private final List<Message> messageList;
    private final String currentUserId; // ID of the user currently viewing the chat

    // View Type Constants
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    // Date Formatter for Timestamp
    // Adjust format as desired (e.g., "HH:mm" for 24-hour)
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    // Constructor now takes the current user's ID
    public ChatAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId; // Store the current user's ID
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        // Check if the message's senderId matches the current user's ID
        if (message != null && message.getSenderId() != null && message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT; // It's a sent message
        } else {
            return VIEW_TYPE_RECEIVED; // It's a received message
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        // Inflate the correct layout based on the view type
        if (viewType == VIEW_TYPE_SENT) {
            view = inflater.inflate(R.layout.item_message_sent, parent, false);
        } else { // VIEW_TYPE_RECEIVED
            view = inflater.inflate(R.layout.item_message_received, parent, false);
        }
        // Pass viewType to ViewHolder constructor to help find correct IDs
        return new MessageViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message == null) return; // Basic null check

        // Bind the message text
        if (holder.messageText != null) {
            holder.messageText.setText(message.getText());
        }

        // Bind the timestamp (format it)
        if (holder.timestampText != null) {
            if (message.getTimestamp() != null) {
                try {
                    holder.timestampText.setText(timeFormat.format(message.getTimestamp().toDate()));
                } catch (IllegalArgumentException e) {
                    // Handle potential date format issues
                    holder.timestampText.setText(""); // Set empty on error
                    android.util.Log.e("ChatAdapter", "Error formatting timestamp", e);
                }
            } else {
                holder.timestampText.setText(""); // Clear timestamp if null
            }
        }

        // Note: We don't set the sender name here anymore as it's implied by layout position
        // If you wanted sender name on received messages, you'd handle it here.
    }

    @Override
    public int getItemCount() {
        return messageList == null ? 0 : messageList.size();
    }

    // --- Updated ViewHolder ---
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        // Views that *might* exist in the layouts
        TextView messageText;
        TextView timestampText;

        // Constructor finds views based on the IDs expected in the specific layout
        public MessageViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            // Find the correct TextView based on the layout inflated
            if (viewType == VIEW_TYPE_SENT) {
                messageText = itemView.findViewById(R.id.message_text_sent);
                timestampText = itemView.findViewById(R.id.timestamp_text_sent);
            } else { // VIEW_TYPE_RECEIVED
                messageText = itemView.findViewById(R.id.message_text_received);
                timestampText = itemView.findViewById(R.id.timestamp_text_received);
            }
        }
    }

    // Optional: Helper method to update the entire list if needed
    public void updateMessages(List<Message> newMessages) {
        this.messageList.clear();
        if (newMessages != null) {
            this.messageList.addAll(newMessages);
        }
        notifyDataSetChanged(); // Consider using DiffUtil for better performance
    }
}