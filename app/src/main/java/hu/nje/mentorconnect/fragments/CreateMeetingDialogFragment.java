package hu.nje.mentorconnect.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import hu.nje.mentorconnect.R;
import hu.nje.mentorconnect.models.Meeting;
import hu.nje.mentorconnect.activities.MapPickerActivity;

public class CreateMeetingDialogFragment extends DialogFragment {

    private TextInputEditText inputTitle, inputPurpose;
    private TextView datePickerText, locationPickerText;
    private Button saveButton;

    private Calendar selectedDateTime;
    private String selectedLocation;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    public static CreateMeetingDialogFragment newInstance(Meeting meeting) {
        CreateMeetingDialogFragment fragment = new CreateMeetingDialogFragment();
        Bundle args = new Bundle();


        args.putString("id", meeting.getId());
        args.putString("title", meeting.getTitle());
        args.putString("location", meeting.getLocation());
        args.putString("purpose", meeting.getPurpose());
        args.putLong("timestamp", meeting.getDate().getSeconds() * 1000);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_meeting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        inputTitle = view.findViewById(R.id.input_meeting_title);
        inputPurpose = view.findViewById(R.id.input_meeting_purpose);
        datePickerText = view.findViewById(R.id.text_date_picker);
        locationPickerText = view.findViewById(R.id.text_location_picker);
        saveButton = view.findViewById(R.id.button_save_meeting);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Prefill fields if editing
        Bundle args = getArguments();
        if (args != null) {
            inputTitle.setText(args.getString("title"));
            inputPurpose.setText(args.getString("purpose"));

            long timestamp = args.getLong("timestamp", 0);
            if (timestamp > 0) {
                selectedDateTime = Calendar.getInstance();
                selectedDateTime.setTimeInMillis(timestamp);
                String formattedDate = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        .format(selectedDateTime.getTime());
                datePickerText.setText(formattedDate);
            }

            selectedLocation = args.getString("location");
            if (selectedLocation != null && !selectedLocation.isEmpty()) {
                locationPickerText.setText("Location: " + selectedLocation);
            }
        }

        datePickerText.setOnClickListener(v -> showDateTimePicker());

        locationPickerText.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MapPickerActivity.class);
            startActivityForResult(intent, 101);
        });

        saveButton.setOnClickListener(v -> saveMeeting());
    }

    private void showDateTimePicker() {
        selectedDateTime = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(getContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePicker = new TimePickerDialog(getContext(),
                            (TimePicker view1, int hourOfDay, int minute) -> {
                                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedDateTime.set(Calendar.MINUTE, minute);

                                String formatted = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                                        .format(selectedDateTime.getTime());
                                datePickerText.setText(formatted);
                            },
                            selectedDateTime.get(Calendar.HOUR_OF_DAY),
                            selectedDateTime.get(Calendar.MINUTE),
                            true);
                    timePicker.show();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == Activity.RESULT_OK && data != null) {
            double lat = data.getDoubleExtra("lat", 0.0);
            double lng = data.getDoubleExtra("lng", 0.0);

            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String locationName = address.getAddressLine(0); // full readable address
                    selectedLocation = locationName;
                    locationPickerText.setText("Location: " + locationName);
                } else {
                    selectedLocation = "Lat: " + lat + ", Lng: " + lng;
                    locationPickerText.setText(selectedLocation);
                }
            } catch (IOException e) {
                selectedLocation = "Lat: " + lat + ", Lng: " + lng;
                locationPickerText.setText(selectedLocation);
            }
        }
    }

    private void saveMeeting() {
        String title = inputTitle.getText().toString().trim();
        String purpose = inputPurpose.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(purpose) ||
                selectedDateTime == null || selectedLocation == null) {
            Toast.makeText(getContext(), "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if we're editing
        String meetingId = getArguments() != null ? getArguments().getString("id") : null;

        Meeting meeting = new Meeting();
        meeting.setTitle(title);
        meeting.setPurpose(purpose);
        meeting.setLocation(selectedLocation);
        meeting.setDate(new Timestamp(selectedDateTime.getTime()));
        meeting.setMentorId(currentUser.getUid());

        if (meetingId != null) {
            // ✅ Edit mode
            db.collection("meetings").document(meetingId)
                    .set(meeting)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Meeting updated!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to update meeting", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // ➕ Add new
            db.collection("meetings")
                    .add(meeting)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(getContext(), "Meeting created!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to create meeting", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}