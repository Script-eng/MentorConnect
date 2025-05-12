package hu.nje.mentorconnect.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;

import hu.nje.mentorconnect.R;

public class ForgotPasswordDialogFragment extends DialogFragment {

    private EditText emailInput;
    private Button sendLinkButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        emailInput = view.findViewById(R.id.email_input);
        sendLinkButton = view.findViewById(R.id.send_link_button);

        sendLinkButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                emailInput.setError("Please enter your email");
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Recovery email sent!", Toast.LENGTH_LONG).show();
                        dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}