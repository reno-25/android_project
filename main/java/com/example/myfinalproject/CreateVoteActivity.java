package com.example.myfinalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateVoteActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1; // Constant for image picker request
    private Uri[] selectedImageUris = new Uri[5]; // Array to store image URIs for each option
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private int selectedImageOptionIndex; // To keep track of which option the user is selecting

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_vote);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Set click listeners for ImageViews
        ImageView option1Image = findViewById(R.id.option1Image);
        option1Image.setOnClickListener(v -> openImagePicker(0));

        ImageView option2Image = findViewById(R.id.option2Image);
        option2Image.setOnClickListener(v -> openImagePicker(1));

        ImageView option3Image = findViewById(R.id.option3Image);
        option3Image.setOnClickListener(v -> openImagePicker(2));

        ImageView option4Image = findViewById(R.id.option4Image);
        option4Image.setOnClickListener(v -> openImagePicker(3));

        ImageView option5Image = findViewById(R.id.option5Image);
        option5Image.setOnClickListener(v -> openImagePicker(4));

        // Set click listener for the submit button
        Button submitVoteButton = findViewById(R.id.submitVoteButton);
        submitVoteButton.setOnClickListener(v -> {
            String voteTitle = ((EditText) findViewById(R.id.voteTitle)).getText().toString();
            List<String> optionsText = new ArrayList<>();
            List<Uri> optionsImages = new ArrayList<>();

            // Collect options text and images
            for (int i = 0; i < 5; i++) {
                EditText optionText = findViewById(getOptionEditTextId(i));
                String option = optionText.getText().toString();
                optionsText.add(option);

                Uri optionImageUri = selectedImageUris[i];
                if (optionImageUri != null) {
                    optionsImages.add(optionImageUri);
                } else {
                    optionsImages.add(null); // If no image selected for this option
                }
            }

            // Validate that the vote title is not empty and at least one option is filled
            if (voteTitle.isEmpty() || optionsText.isEmpty() || optionsText.stream().allMatch(String::isEmpty)) {
                Toast.makeText(this, "Please enter a vote title and at least one option", Toast.LENGTH_SHORT).show();
                return;
            }

            // Upload images and save vote
            uploadImagesAndSaveVote(voteTitle, optionsText, optionsImages);
        });
    }

    // Method to open the image picker
    private void openImagePicker(int optionIndex) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
        selectedImageOptionIndex = optionIndex; // Store the index of the option being edited
    }

    // Handle the result from the image picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            selectedImageUris[selectedImageOptionIndex] = selectedImageUri; // Store the image URI for the selected option

            // Update the ImageView with the selected image
            ImageView optionImageView = findViewById(getImageViewIdForOption(selectedImageOptionIndex));
            optionImageView.setImageURI(selectedImageUri);
        }
    }

    // Helper method to get ImageView ID for each option
    private int getImageViewIdForOption(int optionIndex) {
        switch (optionIndex) {
            case 0:
                return R.id.option1Image;
            case 1:
                return R.id.option2Image;
            case 2:
                return R.id.option3Image;
            case 3:
                return R.id.option4Image;
            case 4:
                return R.id.option5Image;
            default:
                return -1;
        }
    }

    // Helper method to get EditText ID for each option
    private int getOptionEditTextId(int optionIndex) {
        switch (optionIndex) {
            case 0:
                return R.id.option1Text;
            case 1:
                return R.id.option2Text;
            case 2:
                return R.id.option3Text;
            case 3:
                return R.id.option4Text;
            case 4:
                return R.id.option5Text;
            default:
                return -1;
        }
    }

    // Method to upload images and save vote to Firestore
    private void uploadImagesAndSaveVote(String voteTitle, List<String> optionsText, List<Uri> optionsImages) {
        List<Task<Uri>> uploadTasks = new ArrayList<>();
        List<Uri> finalImagesUris = new ArrayList<>(optionsImages); // To store the final image URLs

        // Upload each image to Firebase Storage and get the download URLs
        for (int i = 0; i < optionsImages.size(); i++) {
            Uri imageUri = optionsImages.get(i);
            if (imageUri != null) {
                // Create a reference to Firebase Storage for each image
                StorageReference storageRef = storage.getReference().child("vote_images/" + System.currentTimeMillis());
                int finalI = i; // Need this for the callback

                // Upload the image and get the download URL
                uploadTasks.add(storageRef.putFile(imageUri)
                        .continueWithTask(task -> storageRef.getDownloadUrl()) // Fetch the download URL after upload
                        .addOnSuccessListener(uri -> {
                            // Store the download URL in the finalImagesUris list for the corresponding option
                            finalImagesUris.set(finalI, uri);
                        })
                        .addOnFailureListener(e -> {
                            // Handle image upload failure
                            Toast.makeText(CreateVoteActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }));
            } else {
                // If no image selected for this option, we set the value to null in the list
                finalImagesUris.set(i, null);
            }
        }

        // Once all uploads are complete, store the vote data in Firestore
        Tasks.whenAllComplete(uploadTasks).addOnCompleteListener(task -> {
            // Prepare the vote data to save to Firestore
            Map<String, Object> voteData = new HashMap<>();
            voteData.put("voteTitle", voteTitle);
            voteData.put("optionsText", optionsText);
            voteData.put("optionsImages", finalImagesUris);

            // Save the vote to Firestore in the 'votes' collection
            firestore.collection("votes")  // This will automatically create the 'votes' collection if it doesn't exist
                    .add(voteData)  // Store the vote data
                    .addOnSuccessListener(documentReference -> {
                        // Once the vote is successfully created, notify the user and finish the activity
                        Toast.makeText(CreateVoteActivity.this, "Vote created successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Optionally navigate back to the previous activity or display the vote
                    })
                    .addOnFailureListener(e -> {
                        // If there's an error while saving the vote to Firestore
                        Toast.makeText(CreateVoteActivity.this, "Error creating vote", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
