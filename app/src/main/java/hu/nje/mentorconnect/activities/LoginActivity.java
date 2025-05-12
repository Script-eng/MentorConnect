package hu.nje.mentorconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import hu.nje.mentorconnect.MainActivity;
import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.fragments.ForgotPasswordDialogFragment;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button signInButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        signInButton = findViewById(R.id.register_button);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();



        signInButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();
                            db.collection("users").document(uid).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            String role = documentSnapshot.getString("role");

                                            if ("Mentor".equalsIgnoreCase(role)) {
                                                startActivity(new Intent(this, MentorHomeActivity.class));
                                            } else if ("Mentee".equalsIgnoreCase(role)) {
                                                startActivity(new Intent(this, MainActivity.class));
                                            } else {
                                                Toast.makeText(this, "Unknown role. Contact support.", Toast.LENGTH_SHORT).show();
                                            }
                                            finish();
                                        } else {
                                            Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to fetch user info.", Toast.LENGTH_SHORT).show();
                                    });

                        } else {
                            Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
        TextView textRegister = findViewById(R.id.text_register);
        textRegister.setOnClickListener(v ->{
            startActivity(new Intent(this, RegisterActivity.class));
        });


        TextView recoverPasswordText = findViewById(R.id.text_recover);
        recoverPasswordText.setOnClickListener(v -> {
            ForgotPasswordDialogFragment dialog = new ForgotPasswordDialogFragment();
            dialog.show(getSupportFragmentManager(), "ForgotPasswordDialog");
        });
    }


}