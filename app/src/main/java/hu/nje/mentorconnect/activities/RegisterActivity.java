package hu.nje.mentorconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.nje.mentorconnect.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput, confirmPasswordInput, nameInput;
    private Spinner roleSpinner, mentorListSpinner;
    private Button registerButton;
    private TextView textBackToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<String> mentorNames = new ArrayList<>();
    private List<String> mentorUIDs = new ArrayList<>();
    private String selectedMentorUID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Step 1: Initialize Views
        emailInput = findViewById(R.id.email_input);
        nameInput = findViewById(R.id.name_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        roleSpinner = findViewById(R.id.role_spinner);
        mentorListSpinner = findViewById(R.id.mentor_list);
        registerButton = findViewById(R.id.register_button);
        textBackToLogin = findViewById(R.id.text_login);

        // Step 2: Setup Role Spinner
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.roles_array,
                android.R.layout.simple_spinner_item
        );
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        // Step 3: Manage Role Selection
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRole = roleSpinner.getSelectedItem().toString();

                if ("Mentee".equalsIgnoreCase(selectedRole)) {
                    mentorListSpinner.setVisibility(View.VISIBLE);
                    loadMentorsFromFirestore();
                } else {
                    mentorListSpinner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Step 4: Handle Register Button
        registerButton.setOnClickListener(v -> registerUser());

        // Step 5: Back to Login
        textBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void loadMentorsFromFirestore() {
        mentorNames.clear();
        mentorUIDs.clear();

        db.collection("users")
                .whereEqualTo("role", "Mentor")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            String mentorName = snapshot.getString("name");
                            String mentorUID = snapshot.getString("uid");

                            if (mentorName != null && mentorUID != null) {
                                mentorNames.add(mentorName);
                                mentorUIDs.add(mentorUID);
                            }
                        }

                        ArrayAdapter<String> mentorAdapter = new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_spinner_item,
                                mentorNames
                        );
                        mentorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mentorListSpinner.setAdapter(mentorAdapter);

                        mentorListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedMentorUID = mentorUIDs.get(position);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                selectedMentorUID = null;
                            }
                        });

                    } else {
                        Toast.makeText(this, "No mentors found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load mentors.", Toast.LENGTH_SHORT).show();
                });
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return;
        }
        if (role.equals("Select Role")) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user.getUid();

                        // Save user info to Firestore
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", uid);
                        userMap.put("email", email);
                        userMap.put("name", name);
                        userMap.put("role", role);

                        db.collection("users").document(uid).set(userMap)
                                .addOnSuccessListener(unused -> {
                                    // If mentee, add this mentee to mentor's mentees array
                                    if ("Mentee".equalsIgnoreCase(role) && selectedMentorUID != null) {
                                        Map<String, Object> newMentee = new HashMap<>();
                                        newMentee.put("name", name);
                                        newMentee.put("email", email);

                                        db.collection("users")
                                                .document(selectedMentorUID)
                                                .update("mentees", FieldValue.arrayUnion(newMentee))
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(this, "Mentee linked to mentor successfully!", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(this, LoginActivity.class));
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Failed to link mentee.", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, LoginActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error saving user data", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}