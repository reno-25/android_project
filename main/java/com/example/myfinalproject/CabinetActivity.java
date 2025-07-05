package com.example.myfinalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class CabinetActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView imgCabinet;
    private EditText edtCabinetName, edtVision, edtMission;
    private Button btnUploadImage, btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cabinet);

        // Initialize views
        imgCabinet = findViewById(R.id.img_cabinet);
        edtCabinetName = findViewById(R.id.edt_cabinet_name);
        edtVision = findViewById(R.id.edt_vision);
        edtMission = findViewById(R.id.edt_mission);
        btnUploadImage = findViewById(R.id.btn_upload_image);
        btnSave = findViewById(R.id.btn_save);

        // Open file chooser when button is clicked
        btnUploadImage.setOnClickListener(v -> openFileChooser());

        // Save data when button is clicked
        btnSave.setOnClickListener(v -> {
            String cabinetName = edtCabinetName.getText().toString().trim();
            String vision = edtVision.getText().toString().trim();
            String mission = edtMission.getText().toString().trim();

            if (TextUtils.isEmpty(cabinetName) || TextUtils.isEmpty(vision) || TextUtils.isEmpty(mission)) {
                Toast.makeText(CabinetActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri != null) {
                uploadImage(cabinetName, vision, mission);
            } else {
                Toast.makeText(CabinetActivity.this, "Please upload an image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Open file chooser to select an image
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle image selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgCabinet.setImageURI(imageUri);  // Display the selected image in the ImageView
        }
    }

    // Upload the image to Firebase Storage and save the data to Firestore
    private void uploadImage(String cabinetName, String vision, String mission) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("cabinet_images/" + System.currentTimeMillis() + ".jpg");
        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            // Get the image URL after upload
                            String imageUrl = uri.toString();
                            saveCabinetData(cabinetName, vision, mission, imageUrl);
                        })
                        .addOnFailureListener(e -> Toast.makeText(CabinetActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> Toast.makeText(CabinetActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    // Save cabinet data to Firestore
    private void saveCabinetData(String cabinetName, String vision, String mission, String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Cabinet cabinet = new Cabinet(cabinetName, vision, mission, imageUrl);

        db.collection("cabinet").add(cabinet)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CabinetActivity.this, "Cabinet data saved", Toast.LENGTH_SHORT).show();
                    finish();
                    // Close the activity after saving data
                    startActivity(new Intent(CabinetActivity.this, MainActivity.class));
                })
                .addOnFailureListener(e -> Toast.makeText(CabinetActivity.this, "Failed to save data", Toast.LENGTH_SHORT).show());
    }
}
