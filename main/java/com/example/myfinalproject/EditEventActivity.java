package com.example.myfinalproject;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class EditEventActivity extends AppCompatActivity {

    private EditText editEventName, editEventDate, editEventDescription;
    private Button saveButton;

    private String eventId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Initialize views
        editEventName = findViewById(R.id.editEventName);
        editEventDate = findViewById(R.id.editEventDate);
        editEventDescription = findViewById(R.id.editEventDescription);
        saveButton = findViewById(R.id.saveButton);

        // Get event details passed via intent
        eventId = getIntent().getStringExtra("eventId");
        String eventName = getIntent().getStringExtra("eventName");
        String eventDate = getIntent().getStringExtra("eventDate");
        String eventDescription = getIntent().getStringExtra("eventDescription");

        // Pre-fill the fields with existing data
        editEventName.setText(eventName);
        editEventDate.setText(eventDate);
        editEventDescription.setText(eventDescription);

        // Set a click listener on the EditText for date selection
        editEventDate.setOnClickListener(v -> showDatePickerDialog());

        // Save button logic
        saveButton.setOnClickListener(v -> saveChanges());

        // Set up Toolbar with back button functionality
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // Enable the back arrow
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // Handle the back arrow click
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Go back to EventActivity when back arrow is clicked
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDatePickerDialog() {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create the DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
            // Format the selected date as a string and set it to the EditText
            String formattedDate = selectedDayOfMonth + "/" + (selectedMonth + 1) + "/" + selectedYear;
            editEventDate.setText(formattedDate);
        }, year, month, day);

        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    private void saveChanges() {
        String updatedName = editEventName.getText().toString();
        String updatedDate = editEventDate.getText().toString();
        String updatedDescription = editEventDescription.getText().toString();

        if (updatedName.isEmpty() || updatedDate.isEmpty() || updatedDescription.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update Firestore document
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("events").document(eventId)
                .update(
                        "name", updatedName,
                        "date", updatedDate,
                        "description", updatedDescription
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditEventActivity.this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditEventActivity.this, "Failed to update event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
