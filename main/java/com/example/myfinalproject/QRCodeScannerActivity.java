package com.example.myfinalproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;

import java.util.HashMap;
import java.util.List;

public class QRCodeScannerActivity extends MainActivity {

    private static final String TAG = "QRCodeScannerActivity"; // Debug tag
    private TextView scannedDataText;
    private ProgressBar loadingProgressBar;
    private BarcodeView barcodeScanner;
    private Button scanQRButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        scannedDataText = findViewById(R.id.scannedDataText);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        barcodeScanner = findViewById(R.id.barcode_scanner);
        scanQRButton = findViewById(R.id.btn_scan_qr);

        // Check if camera permission is granted
        checkCameraPermission();

        // Set up the button click listener
        scanQRButton.setOnClickListener(v -> {
            scanQRButton.setVisibility(View.GONE); // Hide the button
            barcodeScanner.setVisibility(View.VISIBLE); // Show the scanner
            startScanner();
        });

        // Initialize the QR code scanner
        barcodeScanner.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null) {
                    String scannedData = result.getText().trim(); // Trim whitespace
                    Log.d(TAG, "Scanned Data: " + scannedData); // Log scanned data for debugging

                    if (!scannedData.isEmpty()) {
                        scannedDataText.setText("Scanned Data: " + scannedData);
                        processScannedData(scannedData);
                        barcodeScanner.pause(); // Pause scanning after a successful scan
                    } else {
                        Toast.makeText(QRCodeScannerActivity.this, "No data found in QR code", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {
                // Handle potential result points (optional)
            }
        });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
                barcodeScanner.resume();
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startScanner() {
        // Show the progress bar and resume the scanner
        loadingProgressBar.setVisibility(View.VISIBLE);
        barcodeScanner.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScanner.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScanner.resume();
    }

    private void processScannedData(String scannedData) {
        // Split the scanned data (format: userId:eventId:email)
        String[] parts = scannedData.split(":");

        if (parts.length == 3) {
            String userId = parts[0];
            String eventId = parts[1];
            String email = parts[2];

            Log.d(TAG, "Parsed Data - UserID: " + userId + ", EventID: " + eventId + ", Email: " + email);

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            // Create the participant data
            HashMap<String, String> participantData = new HashMap<>();
            participantData.put("userId", userId);
            participantData.put("email", email);

            // Store the participant data in the event document
            firestore.collection("events")
                    .document(eventId)
                    .set(new HashMap<String, Object>() {{
                        put("participants." + userId, participantData);
                    }}, SetOptions.merge()) // Merge to avoid overwriting other fields
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Successfully registered attendance", Toast.LENGTH_SHORT).show();
                        loadingProgressBar.setVisibility(View.GONE); // Hide progress bar
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to register attendance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingProgressBar.setVisibility(View.GONE); // Hide progress bar
                    });

        } else {
            Log.e(TAG, "Invalid QR Code Data: " + scannedData);
            Toast.makeText(this, "Invalid QR Code Data", Toast.LENGTH_SHORT).show();
        }
    }
}
