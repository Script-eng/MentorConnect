package hu.nje.mentorconnect.fragments;

// Android & Support Library Imports
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Firebase Imports
import com.google.android.gms.tasks.Task; // Import Task for handling multiple fetches
import com.google.android.gms.tasks.Tasks; // Import Tasks for handling multiple fetches
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.SetOptions;

// Java Util Imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Project Specific Imports
import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.adapters.ChatAdapter;
// Renamed interface, using ChatPartner model
import hu.nje.mentorconnect.adapters.ConversationListAdapter;
import hu.nje.mentorconnect.models.ChatPartner; // Use ChatPartner model for list
import hu.nje.mentorconnect.models.Message;

// Implement the renamed click listener interface
public class ChatFragment extends Fragment implements ConversationListAdapter.OnChatPartnerClickListener {

    private static final String TAG = "ChatFragment";

    // --- UI Elements ---
    private TextView chatTitle;
    private RecyclerView recyclerViewUsers; // Renamed variable for clarity (maps to recyclerViewConversations ID)
    private TextView emptyListText; // Added for empty state
    private ConstraintLayout messageViewContainer;
    private RecyclerView recyclerViewMessages;
    private EditText messageInput;
    private ImageButton sendButton;

    // --- Adapters ---
    private ConversationListAdapter userListAdapter; // Renamed variable for clarity
    private ChatAdapter chatAdapter;

    // --- Data Lists ---
    private List<ChatPartner> chatPartnerList; // List now holds ChatPartner objects
    private List<Message> messageList;

    // --- Firebase ---
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String currentUserId;
    private String currentUserDisplayName;
    private String currentUserRole; // Store user's role
    // No listener needed for the initial user list loading (it's a one-time fetch)
    private ListenerRegistration messagesListener;    // Listener for specific chat messages

    // --- State Management ---
    private boolean isShowingConversationList = true; // Start by showing the list
    private String currentChatRoomId = null;
    private String currentChatPartnerId = null;
    private String currentChatPartnerName = null;

    // --- Back Press Handling ---
    private OnBackPressedCallback backPressedCallback;

    // --- Fragment Lifecycle Methods ---

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            // We'll fetch role and name asynchronously later after DB init
        } else {
            Log.e(TAG, "User not logged in! Cannot initialize ChatFragment.");
            // Handle not logged in state
        }

        // Initialize data lists
        chatPartnerList = new ArrayList<>();
        messageList = new ArrayList<>();

        setupBackPressHandler();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        db = FirebaseFirestore.getInstance();

        if (currentUser == null || currentUserId == null) {
            Log.e(TAG, "User null in onCreateView. Cannot setup view.");
            // Display error message or return empty view
            return view;
        }

        findViews(view); // Find all UI elements
        initializeAdapters(); // Initialize both adapters
        setupRecyclerViews(); // Setup both RecyclerViews
        setupSendButton(); // Setup send button listener

        // Fetch current user details (role, name) and then load partners
        fetchCurrentUserAndLoadPartners();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
        // Back press handler needs to be initialized here AFTER the view is created
        // but ADDED to the dispatcher here.
        if (backPressedCallback == null) { // Initialize if not already done in onCreate
            setupBackPressHandler();
        }
        // Add the callback here, linked to the view's lifecycle
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backPressedCallback);
        // Set initial visibility and back press state
        updateViewVisibility();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // Re-attach message listener ONLY if viewing messages
        if (!isShowingConversationList && currentChatRoomId != null && messagesListener == null) {
            Log.d(TAG,"Re-attaching messages listener in onResume for " + currentChatRoomId);
            loadMessages(currentChatRoomId);
        }
        // Ensure back press handler state is correct
        if (backPressedCallback != null) {
            backPressedCallback.setEnabled(!isShowingConversationList);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        // Detach only the messages listener, user list doesn't need constant listening
        detachMessagesListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        detachMessagesListener(); // Ensure message listener is removed
        // Nullify views and adapters
        recyclerViewUsers = null;
        recyclerViewMessages = null;
        messageViewContainer = null;
        messageInput = null;
        sendButton = null;
        chatTitle = null;
        emptyListText = null;
        userListAdapter = null;
        chatAdapter = null;
        Log.d(TAG,"View references nulled.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        // Remove back press callback
        if (backPressedCallback != null) {
            backPressedCallback.remove();
            backPressedCallback = null;
        }
    }

    // --- Initialization Helpers ---

    private void findViews(View view) {
        chatTitle = view.findViewById(R.id.chatTitle);
        // Use the ID originally meant for conversations list
        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        // Find the new empty text view
        emptyListText = view.findViewById(R.id.empty_list_text); // Make sure this ID exists in fragment_chat.xml
        messageViewContainer = view.findViewById(R.id.messageViewContainer);
        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);

        // Check if emptyListText was found
        if (emptyListText == null) {
            Log.e(TAG, "TextView with ID empty_conversation_text not found in fragment_chat.xml!");
            // Consider adding it or handle the null case gracefully
        }
    }

    private void initializeAdapters() {
        // Adapter for the list of potential chat partners (Mentors/Mentees)
        if (chatPartnerList == null) chatPartnerList = new ArrayList<>();
        userListAdapter = new ConversationListAdapter(chatPartnerList, this); // 'this' implements OnChatPartnerClickListener

        // Adapter for the messages within a specific chat
        if (messageList == null) messageList = new ArrayList<>();
        if (currentUserId == null) Log.e(TAG, "CurrentUserId is null during adapter init!");
        chatAdapter = new ChatAdapter(messageList, currentUserId != null ? currentUserId : "UNKNOWN_USER");
    }

    private void setupRecyclerViews() {
        // User List RecyclerView
        if (recyclerViewUsers != null && userListAdapter != null) {
            recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerViewUsers.setAdapter(userListAdapter);
            recyclerViewUsers.setItemAnimator(null);
        } else {
            Log.e(TAG,"Setup User List RecyclerView failed: View or Adapter is null");
        }

        // Message List RecyclerView
        if (recyclerViewMessages != null && chatAdapter != null) {
            LinearLayoutManager messageLayoutManager = new LinearLayoutManager(getContext());
            messageLayoutManager.setStackFromEnd(true);
            recyclerViewMessages.setLayoutManager(messageLayoutManager);
            recyclerViewMessages.setAdapter(chatAdapter);
            recyclerViewMessages.setItemAnimator(null);
        } else {
            Log.e(TAG,"Setup Message RecyclerView failed: View or Adapter is null");
        }
    }

    private void setupSendButton() {
        if (sendButton != null) {
            sendButton.setOnClickListener(v -> sendMessage());
        } else {
            Log.e(TAG,"SendButton is null in setupSendButton");
        }
    }

    private void fetchCurrentUserAndLoadPartners() {
        if (currentUserId == null || db == null) {
            Log.e(TAG, "Cannot fetch current user, ID or DB is null.");
            showEmptyState("Error loading user data."); // Show error
            return;
        }

        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || documentSnapshot == null || !documentSnapshot.exists()) {
                        Log.e(TAG, "Current user document not found or fragment detached.");
                        showEmptyState("Could not load user profile.");
                        return;
                    }
                    currentUserRole = documentSnapshot.getString("role");
                    currentUserDisplayName = documentSnapshot.getString("name"); // Use 'name' field
                    if (currentUserDisplayName == null || currentUserDisplayName.isEmpty()) {
                        currentUserDisplayName = "Me"; // Fallback
                    }
                    if (currentUserRole == null) {
                        Log.e(TAG, "User role not found for " + currentUserId);
                        showEmptyState("User role is missing.");
                        return;
                    }

                    Log.d(TAG, "Current user role: " + currentUserRole + ", Name: " + currentUserDisplayName);
                    // Now that we have the role, load the appropriate partners
                    loadChatPartners();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch current user data", e);
                    if (isAdded()) {
                        showEmptyState("Error loading user data.");
                    }
                });
    }


    // --- State and Visibility Management ---

    private void updateViewVisibility() {
        if (!isAdded() || recyclerViewUsers == null || messageViewContainer == null || chatTitle == null) {
            Log.w(TAG,"updateViewVisibility skipped: Fragment not added or views are null.");
            return;
        }

        if (isShowingConversationList) {
            recyclerViewUsers.setVisibility(View.VISIBLE);
            messageViewContainer.setVisibility(View.GONE);
            chatTitle.setText(getString(R.string.chat_title_list));
            detachMessagesListener();
            // Empty state handled by loadChatPartners completion
        } else {
            recyclerViewUsers.setVisibility(View.GONE);
            if(emptyListText != null) emptyListText.setVisibility(View.GONE); // Hide empty text when showing messages
            messageViewContainer.setVisibility(View.VISIBLE);
            chatTitle.setText(currentChatPartnerName != null ? currentChatPartnerName : getString(R.string.chat_title_default));
        }
        // Update back press enabled state
        if (backPressedCallback != null) {
            backPressedCallback.setEnabled(!isShowingConversationList);
            Log.d(TAG, "Back press callback enabled: " + !isShowingConversationList);
        }
    }

    private void showConversationList() {
        Log.d(TAG, "Switching to Conversation List View");
        isShowingConversationList = true;
        currentChatRoomId = null;
        currentChatPartnerId = null;
        currentChatPartnerName = null;

        messageList.clear();
        if (chatAdapter != null) chatAdapter.notifyDataSetChanged();

        updateViewVisibility(); // Update UI elements visibility
        // Reload the partner list (it might not change often, but safer)
        loadChatPartners();
        detachMessagesListener(); // Stop listening for specific chat messages
    }

    // --- Callback from UserListAdapter ---
    @Override
    public void onChatPartnerClick(ChatPartner chatPartner) {
        if (chatPartner == null || chatPartner.getUserId() == null) {
            Log.e(TAG, "Invalid chat partner object clicked.");
            showToast("Error starting chat.");
            return;
        }
        Log.d(TAG, "Chat partner clicked: " + chatPartner.getUserId() + ", Name: " + chatPartner.getUserName());
        // Initiate the message view state using the selected partner's info
        startChatWithPartner(chatPartner.getUserId(), chatPartner.getUserName());
    }

    // Renamed from startNewChat for clarity within this flow
    private void startChatWithPartner(String partnerId, String partnerName) {
        if (currentUserId == null || partnerId == null || partnerName == null) {
            Log.e(TAG,"Cannot start chat, missing user/partner info.");
            showToast("Error starting chat.");
            return;
        }
        if (currentUserId.equals(partnerId)){ return; } // Cannot chat with self

        Log.d(TAG, "Starting chat view with Partner ID: " + partnerId + ", Name: " + partnerName);

        this.currentChatRoomId = generateChatRoomId(currentUserId, partnerId);
        if (this.currentChatRoomId == null) {
            showToast("Error starting chat session.");
            return;
        }
        this.currentChatPartnerId = partnerId;
        this.currentChatPartnerName = partnerName;
        this.isShowingConversationList = false;

        updateViewVisibility(); // Show message view UI
        messageList.clear(); // Ensure message list is empty before loading
        if (chatAdapter != null) chatAdapter.notifyDataSetChanged();
        loadMessages(currentChatRoomId); // Load/listen for messages for this chat room
        // Do NOT detach conversation listener here, as we might go back
    }


    // --- Data Loading (Modified) ---

    // Renamed from loadConversations
    private void loadChatPartners() {
        if (currentUserId == null || currentUserRole == null || db == null) {
            Log.e(TAG, "Cannot load chat partners - missing user info or DB.");
            showEmptyState("Could not load users.");
            return;
        }
        Log.d(TAG, "Loading chat partners for role: " + currentUserRole);
        chatPartnerList.clear(); // Clear previous list
        userListAdapter.notifyDataSetChanged(); // Show empty list initially
        showLoadingState(true); // Indicate loading

        if ("Mentee".equalsIgnoreCase(currentUserRole)) {
            loadAssignedMentor();
        } else if ("Mentor".equalsIgnoreCase(currentUserRole)) {
            loadAssignedMentees();
        } else {
            Log.e(TAG, "Unknown user role: " + currentUserRole);
            showEmptyState("Unknown user role.");
            showLoadingState(false);
        }
    }

    private void loadAssignedMentor() {
        Log.d(TAG,"Fetching assigned mentor ID for Mentee: " + currentUserId);
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(menteeDoc -> {
                    if (!isAdded()) return;
                    if (menteeDoc != null && menteeDoc.exists()) {
                        String mentorId = menteeDoc.getString("assignedMentorId"); // Assuming this field exists
                        if (mentorId != null && !mentorId.isEmpty()) {
                            Log.d(TAG,"Found assigned mentor ID: " + mentorId + ". Fetching details...");
                            // Now fetch the mentor's details
                            db.collection("users").document(mentorId).get()
                                    .addOnSuccessListener(mentorDetailsDoc -> {
                                        if (!isAdded()) return;
                                        if (mentorDetailsDoc != null && mentorDetailsDoc.exists()) {
                                            String mentorName = mentorDetailsDoc.getString("name");
                                            if (mentorName != null && !mentorName.isEmpty()) {
                                                chatPartnerList.clear();
                                                chatPartnerList.add(new ChatPartner(mentorId, mentorName));
                                                userListAdapter.notifyDataSetChanged();
                                                showLoadingState(false);
                                                handleEmptyList(); // Hide/show empty text
                                                Log.d(TAG,"Displayed assigned mentor: " + mentorName);
                                            } else {
                                                Log.w(TAG,"Mentor document " + mentorId + " is missing 'name' field.");
                                                showEmptyState("Could not load mentor details.");
                                                showLoadingState(false);
                                            }
                                        } else {
                                            Log.w(TAG,"Assigned mentor document not found: " + mentorId);
                                            showEmptyState("Assigned mentor not found.");
                                            showLoadingState(false);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG,"Failed to fetch mentor details for ID: " + mentorId, e);
                                        if(isAdded()) showEmptyState("Error loading mentor.");
                                        showLoadingState(false);
                                    });
                        } else {
                            Log.w(TAG,"Mentee " + currentUserId + " has no assignedMentorId field or it's empty.");
                            showEmptyState("No mentor assigned.");
                            showLoadingState(false);
                        }
                    } else {
                        Log.w(TAG,"Mentee document not found: " + currentUserId);
                        showEmptyState("Could not load your profile.");
                        showLoadingState(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG,"Failed to fetch mentee document to find mentor ID", e);
                    if(isAdded()) showEmptyState("Error loading data.");
                    showLoadingState(false);
                });
    }

    private void loadAssignedMentees() {
        Log.d(TAG,"Fetching assigned mentee IDs for Mentor: " + currentUserId);
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(mentorDoc -> {
                    if (!isAdded()) return;
                    if (mentorDoc != null && mentorDoc.exists()) {
                        // *** Adapt based on YOUR Firestore structure ***
                        // Option A: If using List<String> assignedMenteeIds
                        List<String> menteeIds = (List<String>) mentorDoc.get("assignedMenteeIds");

                        // Option B: If using List<Map<String, Object>> mentees (with 'uid' inside each map)
                     /*
                     List<Map<String, Object>> menteeMaps = (List<Map<String, Object>>) mentorDoc.get("mentees");
                     List<String> menteeIds = new ArrayList<>();
                     if (menteeMaps != null) {
                         for (Map<String, Object> map : menteeMaps) {
                             if (map != null && map.containsKey("uid")) {
                                 menteeIds.add((String) map.get("uid"));
                             }
                         }
                     }
                     */
                        // *** End Adaptation ***

                        if (menteeIds != null && !menteeIds.isEmpty()) {
                            Log.d(TAG,"Found assigned mentee IDs: " + menteeIds.size() + ". Fetching details...");
                            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                            for (String menteeId : menteeIds) {
                                if (menteeId != null && !menteeId.isEmpty()) {
                                    tasks.add(db.collection("users").document(menteeId).get());
                                }
                            }

                            if (tasks.isEmpty()) {
                                Log.w(TAG,"Mentee ID list was present but empty or contained invalid IDs.");
                                showEmptyState("No valid mentees assigned.");
                                showLoadingState(false);
                                return;
                            }

                            // Wait for all mentee fetches to complete
                            Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                                if (!isAdded()) return;
                                chatPartnerList.clear();
                                for (Object result : results) {
                                    DocumentSnapshot menteeDoc = (DocumentSnapshot) result;
                                    if (menteeDoc.exists()) {
                                        String menteeName = menteeDoc.getString("name");
                                        String id = menteeDoc.getId();
                                        if (menteeName != null && !menteeName.isEmpty()) {
                                            chatPartnerList.add(new ChatPartner(id, menteeName));
                                        } else {
                                            Log.w(TAG,"Mentee document " + id + " missing 'name'.");
                                            chatPartnerList.add(new ChatPartner(id, "Mentee " + id.substring(0, 5))); // Placeholder
                                        }
                                    } else {
                                        // This shouldn't happen if ID was valid, but handle defensively
                                        Log.w(TAG,"Mentee document " + ((DocumentSnapshot) result).getId() + " doesn't exist during fetch.");
                                    }
                                }
                                // Sort by name
                                Collections.sort(chatPartnerList, Comparator.comparing(ChatPartner::getUserName, String.CASE_INSENSITIVE_ORDER));
                                userListAdapter.notifyDataSetChanged();
                                showLoadingState(false);
                                handleEmptyList();
                                Log.d(TAG,"Displayed " + chatPartnerList.size() + " assigned mentees.");
                            }).addOnFailureListener(e -> {
                                Log.e(TAG,"Failed to fetch one or more mentee details", e);
                                if(isAdded()) showEmptyState("Error loading mentees.");
                                showLoadingState(false);
                            });

                        } else {
                            Log.w(TAG,"Mentor " + currentUserId + " has no assigned mentee IDs field or it's empty.");
                            showEmptyState("No mentees assigned.");
                            showLoadingState(false);
                        }
                    } else {
                        Log.w(TAG,"Mentor document not found: " + currentUserId);
                        showEmptyState("Could not load your profile.");
                        showLoadingState(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG,"Failed to fetch mentor document to find mentee IDs", e);
                    if(isAdded()) showEmptyState("Error loading data.");
                    showLoadingState(false);
                });
    }

    // loadMessages - Remains the same (fetches messages for a specific chatRoomId)
    private void loadMessages(String chatRoomId) {
        // ... (Code is identical to previous correct version) ...
        if (chatRoomId == null || db == null) { /* ... */ return; }
        Log.d(TAG, "Loading messages for chatRoomId: " + chatRoomId);
        detachMessagesListener();
        CollectionReference messagesRef = db.collection("chats").document(chatRoomId).collection("messages");
        Query messagesQuery = messagesRef.orderBy("timestamp", Query.Direction.ASCENDING).limitToLast(50);
        messagesListener = messagesQuery.addSnapshotListener((snapshots, error) -> {
            if (!isAdded()) return;
            if (error != null) { Log.e(TAG, "Error listening for messages: ", error); return; }
            if (snapshots == null) { Log.w(TAG, "Messages snapshot null"); return; }
            messageList.clear();
            for(DocumentSnapshot doc : snapshots.getDocuments()){
                try { Message msg = doc.toObject(Message.class); if (msg != null) messageList.add(msg); }
                catch(Exception e) { Log.e(TAG,"Error parsing message: " + doc.getId(), e);}
            }
            Collections.sort(messageList, Comparator.comparingLong(Message::getTimestampMillis));
            if (chatAdapter != null) chatAdapter.notifyDataSetChanged();
            int itemCount = messageList.size();
            if (itemCount > 0 && recyclerViewMessages != null) {
                recyclerViewMessages.post(() -> recyclerViewMessages.smoothScrollToPosition(itemCount - 1));
            }
            Log.d(TAG, "Messages updated. Count: " + itemCount);
        });
    }

    // --- Sending Message (Modified - uses ChatPartner info) ---

    private void sendMessage() {
        // ... (Checks for text, user, partnerId remain the same) ...
        if (messageInput == null || sendButton == null) return;
        String text = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) { return; }
        if (currentUser == null || currentUserId == null) { showToast("Error: Not logged in."); return; }
        if (currentChatPartnerId == null) { showToast("Error: No recipient selected."); return; }
        // Use fetched display names
        if (currentUserDisplayName == null || currentUserDisplayName.isEmpty()) { showToast("Error: Cannot get your name."); return; }
        if (currentChatPartnerName == null || currentChatPartnerName.isEmpty()) { showToast("Error: Cannot get recipient name."); return; }

        final String chatRoomId = generateChatRoomId(currentUserId, currentChatPartnerId);
        if (chatRoomId == null) { showToast("Error creating chat."); return; }

        Log.d(TAG, "Attempting to send message to chatRoomId: " + chatRoomId);

        final DocumentReference chatRoomDocRef = db.collection("chats").document(chatRoomId);
        final CollectionReference messagesColRef = chatRoomDocRef.collection("messages");
        final Message newMessage = new Message(currentUserId, currentChatPartnerId, text);

        sendButton.setEnabled(false); messageInput.setEnabled(false); // Disable input

        chatRoomDocRef.get().addOnCompleteListener(task -> {
            if (!isAdded()) return;
            if (!task.isSuccessful()) { Log.e(TAG, "Error checking chat room existence: ", task.getException()); showToast("Error sending message."); enableInput(); return; }

            DocumentSnapshot document = task.getResult();
            WriteBatch batch = db.batch();
            boolean isNewChat = (document == null || !document.exists());

            Map<String, Object> chatData = new HashMap<>();
            chatData.put("lastMessageText", text);
            chatData.put("lastMessageTimestamp", FieldValue.serverTimestamp());
            chatData.put("lastMessageSenderId", currentUserId);

            if (isNewChat) {
                Log.d(TAG, "Creating new chat room document: " + chatRoomId);
                List<String> participants = Arrays.asList(currentUserId, currentChatPartnerId);
                Map<String, String> userNames = new HashMap<>();
                // Use the stored display names
                userNames.put(currentUserId, currentUserDisplayName);
                userNames.put(currentChatPartnerId, currentChatPartnerName);
                chatData.put("participants", participants);
                chatData.put("userNames", userNames);
                batch.set(chatRoomDocRef, chatData); // Create the document
            } else {
                Log.d(TAG, "Updating existing chat room document: " + chatRoomId);
                batch.update(chatRoomDocRef, chatData); // Update existing
            }
            batch.set(messagesColRef.document(), newMessage); // Add the message

            batch.commit()
                    .addOnSuccessListener(aVoid -> { Log.i(TAG, "Message sent & chat room processed: " + chatRoomId); messageInput.setText(""); })
                    .addOnFailureListener(e -> { Log.e(TAG, "Error committing message batch: " + chatRoomId, e); showToast("Failed to send message."); })
                    .addOnCompleteListener(commitTask -> { if(isAdded()) enableInput(); });
        });
    }

    // enableInput - Remains the same
    private void enableInput() { /* ... */ if(isAdded() && messageInput != null && sendButton != null) { messageInput.setEnabled(true); sendButton.setEnabled(true); } }

    // generateChatRoomId - Remains the same
    private String generateChatRoomId(String uid1, String uid2) { /* ... */ if (uid1 == null || uid2 == null) {return null;} if (uid1.compareTo(uid2) < 0) { return uid1 + "_" + uid2; } else if (uid1.compareTo(uid2) > 0) { return uid2 + "_" + uid1; } else { return null; } }

    // --- Listener and Callback Handling ---
    private void setupBackPressHandler() {
        // ... (code remains the same) ...
        backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                if (!isShowingConversationList) {
                    showConversationList();
                }
            }
        };
        // Don't add to dispatcher here, do it in onViewCreated
        Log.d(TAG,"Back press handler initialized.");
    }

    private void detachMessagesListener() {
        // ... (code remains the same) ...
        if (messagesListener != null) { messagesListener.remove(); messagesListener = null; Log.d(TAG, "Detached messages listener."); }
    }
    // No need for detachConversationsListener as we don't keep it active

    // --- UI Helpers ---
    private void showLoadingState(boolean isLoading) {
        // TODO: Implement visual loading indicator (e.g., ProgressBar)
        Log.d(TAG, "Setting loading state: " + isLoading);
        // Example: progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (recyclerViewUsers != null) {
            // Optionally hide list while loading to prevent showing old data
            // recyclerViewUsers.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }
    }

    private void showEmptyState(String message) {
        if (!isAdded() || emptyListText == null || recyclerViewUsers == null) return;
        Log.d(TAG, "Showing empty state: " + message);
        recyclerViewUsers.setVisibility(View.GONE);
        emptyListText.setVisibility(View.VISIBLE);
        emptyListText.setText(message != null ? message : "No users found.");
    }

    private void handleEmptyList() {
        if (!isAdded() || chatPartnerList == null || emptyListText == null || recyclerViewUsers == null) return;
        if (chatPartnerList.isEmpty()) {
            showEmptyState("No assigned users found.");
        } else {
            emptyListText.setVisibility(View.GONE);
            recyclerViewUsers.setVisibility(View.VISIBLE);
        }
    }


    private void showToast(String message) {
        if (isAdded() && getContext() != null && message != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

} // --- End ChatFragment Class ---