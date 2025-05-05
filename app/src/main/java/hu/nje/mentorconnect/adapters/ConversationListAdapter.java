package hu.nje.mentorconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.models.Conversation;

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.ConversationViewHolder> {

    private final List<Conversation> conversationList;
    private final OnConversationClickListener clickListener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

    // Interface for handling clicks
    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationListAdapter(List<Conversation> conversationList, OnConversationClickListener listener) {
        this.conversationList = conversationList;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);
        holder.bind(conversation, clickListener, timeFormat, dateFormat);
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView partnerNameText;
        TextView lastMessageText;
        TextView lastMessageTimeText;

        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            partnerNameText = itemView.findViewById(R.id.partner_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTimeText = itemView.findViewById(R.id.last_message_time_text);
        }

        void bind(final Conversation conversation, final OnConversationClickListener listener, SimpleDateFormat timeFormat, SimpleDateFormat dateFormat) {
            partnerNameText.setText(conversation.getPartnerName() != null ? conversation.getPartnerName() : "Unknown User");
            lastMessageText.setText(conversation.getLastMessageText() != null ? conversation.getLastMessageText() : "");

            // Format the timestamp
            if (conversation.getLastMessageTimestamp() != null) {
                long timeMillis = conversation.getLastMessageTimestampMillis();
                // Basic logic: Show time if today, show date if older
                // You might want more sophisticated logic (yesterday, etc.)
                if (isToday(timeMillis)) {
                    lastMessageTimeText.setText(timeFormat.format(conversation.getLastMessageTimestamp().toDate()));
                } else {
                    lastMessageTimeText.setText(dateFormat.format(conversation.getLastMessageTimestamp().toDate()));
                }
            } else {
                lastMessageTimeText.setText("");
            }

            // Set click listener for the whole item view
            itemView.setOnClickListener(v -> listener.onConversationClick(conversation));
        }

        // Helper to check if timestamp is today
        private static boolean isToday(long timeMillis) {
            long currentTime = System.currentTimeMillis();
            // Basic check by comparing day, month, year
            SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            return dayFormat.format(timeMillis).equals(dayFormat.format(currentTime));
        }
    }

    // Helper method to update the entire list
    public void updateConversations(List<Conversation> newConversations) {
        this.conversationList.clear();
        this.conversationList.addAll(newConversations);
        notifyDataSetChanged();
    }
}
