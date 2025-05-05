package hu.nje.mentorconnect.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Using ChatPartner model now
import java.util.List;

import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.models.ChatPartner; // Import the new model

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.ConversationViewHolder> {

    // Changed List type to ChatPartner
    private final List<ChatPartner> chatPartnerList;
    private final OnChatPartnerClickListener clickListener;

    // Interface for handling clicks - renamed for clarity
    public interface OnChatPartnerClickListener {
        void onChatPartnerClick(ChatPartner chatPartner); // Passes ChatPartner object
    }

    // Constructor takes List<ChatPartner> and the renamed listener interface
    public ConversationListAdapter(List<ChatPartner> chatPartnerList, OnChatPartnerClickListener listener) {
        this.chatPartnerList = chatPartnerList;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Still uses item_conversation layout, as it displays similar info (name)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        ChatPartner chatPartner = chatPartnerList.get(position);
        holder.bind(chatPartner, clickListener); // Pass ChatPartner to bind method
    }

    @Override
    public int getItemCount() {
        return chatPartnerList == null ? 0 : chatPartnerList.size();
    }

    // --- ViewHolder ---
    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        // Views expected in item_conversation.xml
        TextView partnerNameText;
        TextView lastMessageText;       // We might hide or repurpose this
        TextView lastMessageTimeText;   // We might hide or repurpose this

        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views from item_conversation.xml
            partnerNameText = itemView.findViewById(R.id.partner_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTimeText = itemView.findViewById(R.id.last_message_time_text);
        }

        // Bind method now takes a ChatPartner object
        void bind(final ChatPartner chatPartner, final OnChatPartnerClickListener listener) {
            // Display the partner's name
            partnerNameText.setText(chatPartner.getUserName() != null ? chatPartner.getUserName() : "Unknown User");

            // --- Decide what to do with the other TextViews ---
            // Option A: Hide them if they are irrelevant now
            if (lastMessageText != null) lastMessageText.setVisibility(View.GONE);
            if (lastMessageTimeText != null) lastMessageTimeText.setVisibility(View.GONE);

            // Option B: Repurpose them (e.g., show User Role if you add it to ChatPartner)
            // if (lastMessageText != null && chatPartner.getUserRole() != null) {
            //     lastMessageText.setVisibility(View.VISIBLE);
            //     lastMessageText.setText(chatPartner.getUserRole());
            // } else if (lastMessageText != null) {
            //      lastMessageText.setVisibility(View.GONE);
            // }
            // if (lastMessageTimeText != null) lastMessageTimeText.setVisibility(View.GONE); // Hide time for now

            // Set click listener for the whole item view
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChatPartnerClick(chatPartner); // Pass the clicked ChatPartner back
                }
            });
        }
    } // --- End ViewHolder ---

    // Helper method to update the list (now takes List<ChatPartner>)
    public void updateChatPartners(List<ChatPartner> newChatPartners) {
        this.chatPartnerList.clear();
        if (newChatPartners != null) {
            this.chatPartnerList.addAll(newChatPartners);
        }
        notifyDataSetChanged(); // Use DiffUtil for better performance later
    }
}