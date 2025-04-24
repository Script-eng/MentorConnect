package hu.nje.mentorconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import hu.nje.mentorconnect.MainActivity;
import hu.nje.mentorconnect.R;

public class LoginIntroActivity extends AppCompatActivity {

    private Button signInButton, registerButton;
    private TextView skipText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_intro);

        signInButton = findViewById(R.id.button_sign_in);
        registerButton = findViewById(R.id.button_register);
        skipText = findViewById(R.id.text_skip);

        signInButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginIntroActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        registerButton.setOnClickListener(v -> {
            // For now, simulate a toast or redirect to LoginActivity as placeholder
            Intent intent = new Intent(LoginIntroActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        skipText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginIntroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}