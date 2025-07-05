package com.example.myfinalproject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editUsername, editEmail;
    private Button saveButton;
    private ImageView profileImage;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize UI components
        editUsername = findViewById(R.id.edit_username);
        editEmail = findViewById(R.id.edit_email);
        saveButton = findViewById(R.id.save_button);
        profileImage = findViewById(R.id.profile_image);

        // Load current user data
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            editUsername.setText(currentUser.getDisplayName());
            editEmail.setText(currentUser.getEmail());
        }

        // Set save button onClickListener
        saveButton.setOnClickListener(v -> {
            String newUsername = editUsername.getText().toString().trim();
            String newEmail = editEmail.getText().toString().trim();

            // Check if all fields are filled
            if (TextUtils.isEmpty(newUsername) || TextUtils.isEmpty(newEmail)) {
                Toast.makeText(EditProfileActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save user profile data
            saveUserProfile(newUsername, newEmail);
        });

        // Set profile image onClickListener to choose a new photo
        profileImage.setOnClickListener(v -> {
            openImagePicker();
        });
    }

    private void saveUserProfile(String username, String email) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("username", username);
            userMap.put("email", email);

            // Save to Firestore
            firestore.collection("users").document(userId).update(userMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            // Optionally update email (if necessary)
            currentUser.updateEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, "Email updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);  // Set the selected image to ImageView

            // Optionally, upload the image to Firebase Storage
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference storageRef = storage.getReference().child("profile_images").child(auth.getCurrentUser().getUid());
            storageRef.putFile(imageUri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Get the download URL of the uploaded image
                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Save the image URL in Firestore
                                FirebaseUser currentUser = auth.getCurrentUser();
                                if (currentUser != null) {
                                    String userId = currentUser.getUid();
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("profileImage", uri.toString()); // Save image URL

                                    firestore.collection("users").document(userId).update(userMap)
                                            .addOnCompleteListener(updateTask -> {
                                                if (updateTask.isSuccessful()) {
                                                    Toast.makeText(EditProfileActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(EditProfileActivity.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
