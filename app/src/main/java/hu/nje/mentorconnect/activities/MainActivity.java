package hu.nje.mentorconnect.activities;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.fragments.ChatFragment;
import hu.nje.mentorconnect.fragments.MapFragment;
import hu.nje.mentorconnect.fragments.MentorListFragment;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);
        loadFragment(new MentorListFragment()); // Load default

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int id = item.getItemId();

                if (id == R.id.nav_mentors) {
                    selectedFragment = new MentorListFragment();
                } else if (id == R.id.nav_chat) {
                    selectedFragment = new ChatFragment();
                } else if (id == R.id.nav_map) {
                    selectedFragment = new MapFragment();
                }

                return loadFragment(selectedFragment);
            }
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}