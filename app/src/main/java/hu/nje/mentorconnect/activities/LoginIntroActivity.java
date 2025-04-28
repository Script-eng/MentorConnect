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

    Button btnSignIn, btnRegister;
    TextView textSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_intro);

        btnSignIn = findViewById(R.id.button_sign_in);
        btnRegister = findViewById(R.id.button_register);
        textSkip = findViewById(R.id.text_skip);

        btnSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        btnRegister.setOnClickListener(v -> {
            // Temporary: Use same Login screen
            startActivity(new Intent(this, LoginActivity.class));
        });

        textSkip.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish(); // prevent coming back to intro
        });
    }
}