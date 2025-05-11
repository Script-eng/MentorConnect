package hu.nje.mentorconnect.fragments;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.adapters.MenteeAdapter;
import hu.nje.mentorconnect.models.Mentee;

public class MenteesFragment extends Fragment {

    private RecyclerView recyclerView;
    private MenteeAdapter adapter;
    private List<Mentee> menteeList;
    private FirebaseFirestore db;
    private String currentUserId;

    public MenteesFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mentees, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.mentee_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        menteeList = new ArrayList<>();
        adapter = new MenteeAdapter(menteeList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadMentees();
    }

    private void loadMentees() {
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(mentorDoc -> {
                    String mentorName = mentorDoc.getString("name");
                    if (mentorName == null) mentorName = "Unknown Mentor";

                    String finalMentorName = mentorName;
                    db.collection("users")
                            .whereEqualTo("role", "Mentee")
                            .get()
                            .addOnSuccessListener(querySnapshots -> {
                                menteeList.clear();
                                for (QueryDocumentSnapshot doc : querySnapshots) {
                                    String name = doc.getString("name");
                                    String email = doc.getString("email");
                                    String assignedMentorId = doc.getString("assignedMentorId");

                                    // Only add mentees assigned to this mentor
                                    if (assignedMentorId != null && assignedMentorId.equals(currentUserId)) {
                                        menteeList.add(new Mentee(name, email, finalMentorName));
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("MenteesFragment", "Error loading mentees", e);
                                Toast.makeText(getContext(), "Failed to load mentees", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("MenteesFragment", "Error fetching mentor name", e);
                    Toast.makeText(getContext(), "Failed to fetch mentor info", Toast.LENGTH_SHORT).show();
                });
    }
}