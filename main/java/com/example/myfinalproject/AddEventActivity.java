package com.example.myfinalproject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddEventActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "AddEventActivity";

    private ImageView eventImageView;
    private EditText eventNameEditText, eventDateEditText, eventDescriptionEditText;
    private Button selectImageButton, saveEventButton;

    private Uri imageUri;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_event_activity);

        // Initialize Firebase
        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI components
        eventImageView = findViewById(R.id.eventImageView);
        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventDateEditText = findViewById(R.id.eventDateEditText);
        eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
        selectImageButton = findViewById(R.id.selectImageButton);
        saveEventButton = findViewById(R.id.saveEventButton);

        // Set up the Toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Display the back button
        }

        // Open file chooser for image selection
        selectImageButton.setOnClickListener(v -> openFileChooser());

        // Save event details
        saveEventButton.setOnClickListener(v -> saveEvent());

        // Show DatePickerDialog when date field is clicked
        eventDateEditText.setOnClickListener(v -> showDatePickerDialog());
    }

    // Handle the back button press
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Go back to the EventActivity
        return true;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            eventImageView.setImageURI(imageUri); // Display selected image
        }
    }

    private void saveEvent() {
        String eventName = eventNameEditText.getText().toString().trim();
        String eventDate = eventDateEditText.getText().toString().trim();
        String eventDescription = eventDescriptionEditText.getText().toString().trim();

        if (imageUri == null || eventName.isEmpty() || eventDate.isEmpty() || eventDescription.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload image to Firebase Storage
        StorageReference storageRef = storage.getReference().child("event_images/" + System.currentTimeMillis() + ".jpg");
        storageRef.putFile(imageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();

                    // Save event details in Firestore
                    Map<String, Object> event = new HashMap<>();
                    event.put("name", eventName);
                    event.put("date", eventDate);
                    event.put("description", eventDescription);
                    event.put("imageUrl", imageUrl);

                    firestore.collection("events").add(event).addOnCompleteListener(eventTask -> {
                        if (eventTask.isSuccessful()) {
                            Toast.makeText(AddEventActivity.this, "Event added successfully!", Toast.LENGTH_SHORT).show();
                            // Navigate to EventActivity
                            Intent intent = new Intent(AddEventActivity.this, EventActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Exception exception = eventTask.getException();
                            String errorMessage = (exception != null) ? exception.getMessage() : "Unknown error occurred.";
                            Toast.makeText(AddEventActivity.this, "Firestore Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Firestore Error", exception);
                        }
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(AddEventActivity.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Storage URL Error", e);
                });
            } else {
                Exception exception = task.getException();
                String errorMessage = (exception != null) ? exception.getMessage() : "Unknown error occurred.";
                Toast.makeText(AddEventActivity.this, "Image upload failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Storage Upload Error", exception);
            }
        });
    }

    // Show the DatePickerDialog to select a date
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            // Format and set the date to the EditText
            String formattedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            eventDateEditText.setText(formattedDate);
        }, year, month, day);

        datePickerDialog.show();
    }
}
