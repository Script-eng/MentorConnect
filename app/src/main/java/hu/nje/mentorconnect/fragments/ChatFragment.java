package hu.nje.mentorconnect.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.adapters.ChatAdapter;
import hu.nje.mentorconnect.models.Message;

public class ChatFragment extends Fragment {

    private EditText messageInput;
    private ImageButton sendButton;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;

    private FirebaseFirestore db;
    private CollectionReference chatRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);
        recyclerView = view.findViewById(R.id.recyclerViewMessages);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(chatAdapter);

        db = FirebaseFirestore.getInstance();
        chatRef = db.collection("messages");

        // Load existing messages
        loadMessages();

        // Send new message
        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                Message message = new Message("mentee", text, System.currentTimeMillis());
                chatRef.add(message);
                messageInput.setText("");
            }
        });

        return view;
    }

    private void loadMessages() {
        chatRef.addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;

            messageList.clear();
            for (QueryDocumentSnapshot doc : value) {
                Message msg = doc.toObject(Message.class);
                messageList.add(msg);
            }

            // Sort by timestamp
            Collections.sort(messageList, Comparator.comparingLong(Message::getTimestamp));
            chatAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(messageList.size() - 1);
        });
    }
}