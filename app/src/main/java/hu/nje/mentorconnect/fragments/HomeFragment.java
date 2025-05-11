package hu.nje.mentorconnect.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.activities.LoginActivity;

public class HomeFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        View cardMentors = view.findViewById(R.id.card_mentors);
        View cardChat = view.findViewById(R.id.card_chat);
        View cardDocs = view.findViewById(R.id.card_documents);
        TextView cardMentorsText = view.findViewById(R.id.text_card_mentors);


        // Handle Logout icon in the Toolbar
        Toolbar toolbar = view.findViewById(R.id.home_toolbar);
        toolbar.setNavigationOnClickListener(v -> showLogoutDialog());




        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        boolean isMentor = doc.exists() && "Mentor".equalsIgnoreCase(doc.getString("role"));

                        if (isMentor) {
                            cardMentorsText.setText("View Mentees");
                            cardMentors.setOnClickListener(v -> {
                                navigateToFragment(new MenteesFragment(), R.id.nav_mentees);
                            });
                        } else {
                            cardMentorsText.setText("View Mentors");
                            cardMentors.setOnClickListener(v -> {
                                navigateToFragment(new MentorFragment(), R.id.nav_mentors);
                            });
                        }
                    });
        }

        cardChat.setOnClickListener(v -> {
            navigateToFragment(new ChatFragment(), R.id.nav_chat);
        });

        cardDocs.setOnClickListener(v -> {
            navigateToFragment(new DocsFragment(), R.id.nav_documents);
        });
    }

    private void navigateToFragment(Fragment fragment, int menuItemId) {
        BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(menuItemId); // updates nav selection

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Do you really want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}