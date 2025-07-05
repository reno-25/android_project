package com.example.myfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import com.bumptech.glide.Glide;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName, profileEmail;
    private ImageView profileImage;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileImage = findViewById(R.id.profile_image);  // Pastikan ini ada untuk mendapatkan ImageView


        // Find the Edit Profile button by ID
        Button editProfileButton = findViewById(R.id.edit_profile_picture);

        // Set OnClickListener to navigate to EditProfileActivity
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Set up the Toolbar and Drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set NavigationView Listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_event) {
                    Toast.makeText(ProfileActivity.this, "Navigating to Event", Toast.LENGTH_SHORT).show();
                    // Navigate to ProfileActivity (implement if required)
                    // startActivity(new Intent(EventActivity.this, ProfileActivity.class));
                } else if (id == R.id.nav_profile) {
                    Toast.makeText(ProfileActivity.this, "Already in Profile", Toast.LENGTH_SHORT).show();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

        });




        // Initialize UI components
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_info);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Get current user
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();


            // Fetch user data from Firestore
            firestore.collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Retrieve user data
                                String username = document.getString("username");
                                String email = document.getString("email");
                                String profileImageUrl = document.getString("profileImage");

                                // Set data to UI
                                profileName.setText(username);
                                profileEmail.setText(email);

                                // Sebelum memuat gambar dengan Glide
                                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                    Glide.with(ProfileActivity.this)
                                            .load(profileImageUrl)  // URL gambar yang valid dari Firestore
                                            .placeholder(R.drawable.default_profile)  // Gambar placeholder
                                            .into(profileImage);  // Set gambar ke ImageView
                                } else {
                                    profileImage.setImageResource(R.drawable.default_profile);  // Gambar default jika URL null atau kosong
                                }

                            } else {
                                Toast.makeText(ProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to fetch user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}
