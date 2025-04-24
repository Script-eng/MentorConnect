package hu.nje.mentorconnect.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.adapters.MentorAdapter;
import hu.nje.mentorconnect.models.Mentor;

public class MentorFragment extends Fragment {

    private List<Mentor> mentors;
    private RecyclerView recyclerView;
    private MentorAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mentor_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.mentor_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mentors = new ArrayList<>();

        // ✅ This is where you add the sample data
        mentors.add(new Mentor("Aliz Kovacs", "Computer Science", R.drawable.ic_mentor));
        mentors.add(new Mentor("Salah Ben Sarar", "Electrical Engineering", R.drawable.ic_mentor));
        mentors.add(new Mentor("Setphan Kilouso", "Mechanical Engineering", R.drawable.ic_mentor));

        adapter = new MentorAdapter(mentors);
        recyclerView.setAdapter(adapter);
    }
}