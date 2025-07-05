package com.example.myfinalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Firebase and UI components
    private FirebaseAuth auth;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private ImageView imgInstagram, imgWhatsApp, imgYouTube;
    private Button btnNewCabinet;
    private CabinetAdapter adapter;
    private List<Cabinet> cabinetList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("cabinetData");

        // Set up DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
        recyclerView = findViewById(R.id.cabinet_recycler_view);

        // Set up the toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up ActionBarDrawerToggle for the drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set up the NavigationView listener for menu items
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                // Handle different menu item clicks
                if (id == R.id.nav_profile) {
                    // Navigate to profile
                    Toast.makeText(MainActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                } else if (id == R.id.nav_event) {
                    // Navigate to events
                    Toast.makeText(MainActivity.this, "Events clicked", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, EventActivity.class));
                } else if (id == R.id.nav_scanner) {
                    // Navigate to QR Scanner
                    startActivity(new Intent(MainActivity.this, QRCodeScannerActivity.class));
                } else if (id == R.id.nav_voting) {
                    // Navigate to QR Scanner
                    startActivity(new Intent(MainActivity.this, VoteActivity.class));
                } else if (id == R.id.nav_logout) {
                    // Log out user
                    logoutUser();
                }

                // Close the drawer after selecting an item
                drawerLayout.closeDrawers();
                return true;
            }
        });

        // Initialize RecyclerView and Adapter
        cabinetList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CabinetAdapter(cabinetList);
        recyclerView.setAdapter(adapter);

        loadCabinetData();

        // Social media links
        imgInstagram = findViewById(R.id.img_instagram);
        imgWhatsApp = findViewById(R.id.img_whatsapp);
        imgYouTube = findViewById(R.id.img_youtube);

        imgInstagram.setOnClickListener(view -> openAppOrLink("com.instagram.android", "https://www.instagram.com/ispresuniv?igsh=MTdzaWV0MjJoZjBreA=="));
        imgWhatsApp.setOnClickListener(view -> openAppOrLink("com.whatsapp", "https://chat.whatsapp.com/LJq0sOXiUlAJXs2cEncGmK"));
        imgYouTube.setOnClickListener(view -> openAppOrLink("com.google.android.youtube", "https://youtube.com/@ispresuniv?si=9zLmJ-Gky2_g0c3Z"));

        // Edit button
        btnNewCabinet = findViewById(R.id.btn_new_cabinet);
        btnNewCabinet.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CabinetActivity.class);
            startActivity(intent);
        });
    }

    // Load data from Firestore
    private void loadCabinetData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cabinet")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cabinetList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Cabinet cabinet = document.toObject(Cabinet.class);
                            cabinet.setDocumentId(document.getId()); // Simpan ID dokumen
                            cabinetList.add(cabinet);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MainActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to open app or link in browser
    private void openAppOrLink(String packageName, String link) {
        // Open app or link for Instagram, WhatsApp, YouTube
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        }
        startActivity(intent);
    }

    // Logout user
    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}
