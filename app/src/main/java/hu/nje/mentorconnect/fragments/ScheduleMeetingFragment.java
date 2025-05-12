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
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.adapters.MeetingAdapter;
import hu.nje.mentorconnect.models.Meeting;

public class ScheduleMeetingFragment extends Fragment {

    private RecyclerView meetingRecyclerView;
    private MeetingAdapter meetingAdapter;
    private List<Meeting> meetingList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private boolean isMentor = false;

    public ScheduleMeetingFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule_meeting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        meetingRecyclerView = view.findViewById(R.id.recycler_meetings);
        meetingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        meetingAdapter = new MeetingAdapter(meetingList, isMentor, new MeetingAdapter.OnMeetingActionListener() {
            @Override
            public void onEdit(Meeting meeting) {
                CreateMeetingDialogFragment dialog = CreateMeetingDialogFragment.newInstance(meeting);
                dialog.show(getChildFragmentManager(), "EditMeetingDialog");
            }

            @Override
            public void onDelete(Meeting meeting) {
                db.collection("meetings")
                        .document(meeting.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Meeting deleted", Toast.LENGTH_SHORT).show();
                            fetchMeetings(); // Refresh list
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                        });
            }
        });
        meetingRecyclerView.setAdapter(meetingAdapter);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        FloatingActionButton fab = view.findViewById(R.id.fab_add_meeting);

        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String role = snapshot.getString("role");
                    isMentor = "Mentor".equalsIgnoreCase(role);
                    meetingAdapter.setMentor(isMentor);

                    if (isMentor) {
                        fab.setVisibility(View.VISIBLE);
                        fab.setOnClickListener(v -> {
                            CreateMeetingDialogFragment dialog = CreateMeetingDialogFragment.newInstance(new Meeting());
                            dialog.show(getChildFragmentManager(), "CreateMeetingDialog");



                        });
                    } else {
                        fab.setVisibility(View.GONE);
                    }

                    fetchMeetings();
                }
            }).addOnFailureListener(e -> {
                Log.e("ScheduleMeetingFragment", "Error checking role", e);
                Toast.makeText(getContext(), "Failed to check role", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void fetchMeetings() {
        db.collection("meetings").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    meetingList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Meeting meeting = doc.toObject(Meeting.class);
                        meeting.setId(doc.getId());
                        meetingList.add(meeting);
                    }
                    meetingAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("ScheduleMeetingFragment", "Error loading meetings", e);
                    Toast.makeText(getContext(), "Failed to load meetings", Toast.LENGTH_SHORT).show();
                });
    }
}