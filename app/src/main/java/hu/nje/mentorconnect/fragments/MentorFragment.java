package hu.nje.mentorconnect.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.adapters.MentorAdapter;
import hu.nje.mentorconnect.models.Mentor;

public class MentorFragment extends Fragment {

    private RecyclerView recyclerView;
    private MentorAdapter adapter;
    private List<Mentor> mentorList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    public MentorFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mentor_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.mentor_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mentorList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        adapter = new MentorAdapter(mentorList, mentor -> {
            if (currentUser == null) {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            // For demo: If mentor UID matches a hardcoded assigned mentor ID, open chat
            // Replace with actual Firestore lookup of user's assigned mentor
            if (mentor.getUid().equals("ASSIGNED_MENTOR_UID")) {
                Toast.makeText(getContext(), "Open Chat with " + mentor.getName(), Toast.LENGTH_SHORT).show();
                // Navigate to ChatFragment or Activity if implemented
            } else {
                // Launch email intent
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + mentor.getEmail()));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Mentor Inquiry");
                startActivity(Intent.createChooser(intent, "Contact Mentor via Email"));
            }
        });

        recyclerView.setAdapter(adapter);
        fetchMentorsFromFirestore();
    }

    private void fetchMentorsFromFirestore() {
        db.collection("users")
                .whereEqualTo("role", "Mentor")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    mentorList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String name = doc.getString("name");
                        String department = doc.getString("department");
                        String email = doc.getString("email");
                        String uid = doc.getString("uid");

                        if (name != null && department != null && email != null && uid != null) {
                            mentorList.add(new Mentor(name, department, email, uid));
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("MentorFragment", "Error fetching mentors", e);
                    Toast.makeText(getContext(), "Failed to load mentors", Toast.LENGTH_SHORT).show();
                });
    }
}