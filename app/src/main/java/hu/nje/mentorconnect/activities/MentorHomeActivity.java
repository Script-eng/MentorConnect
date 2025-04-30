package hu.nje.mentorconnect.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import hu.nje.mentorconnect.R;

public class MentorHomeActivity extends AppCompatActivity {

    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentor_home);

        welcomeText = findViewById(R.id.welcome_text);
        welcomeText.setText("Welcome, Mentor!");
    }
}