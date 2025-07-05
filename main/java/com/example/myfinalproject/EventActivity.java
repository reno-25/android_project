package com.example.myfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class    EventActivity extends MainActivity {

    private RecyclerView eventRecyclerView;
    private EventAdapter eventAdapter;
    private FirebaseFirestore firestore;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Button addEventButton; // Declare the Button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_activity);

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

                // Handle different menu item clicks
                if (id == R.id.nav_profile) {
                    // Handle navigation to profile
                    Toast.makeText(EventActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
                    // Open Profile Activity (you can create this activity)
                    // startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                } else if (id == R.id.nav_event) {
                    // Handle navigation to events
                    Toast.makeText(EventActivity.this, "Events clicked", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EventActivity.this, EventActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_scanner) {
                    // Handle navigation to QR Scanner
                    Intent intent = new Intent(EventActivity.this, QRCodeScannerActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_logout) {
                    // Handle log out
                    logoutUser();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // Set up RecyclerView
        eventRecyclerView = findViewById(R.id.eventRecyclerView);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(this, new ArrayList<>());
        eventRecyclerView.setAdapter(eventAdapter);

        // Load events from Firestore
        loadEventsFromFirestore();

        // Initialize the Add Event Button and set the OnClickListener
        addEventButton = findViewById(R.id.addEventButton);
        addEventButton.setOnClickListener(v -> {
            // Open AddEventActivity when the button is clicked
            Intent intent = new Intent(EventActivity.this, AddEventActivity.class);
            startActivity(intent);
        });
    }

    private void loadEventsFromFirestore() {
        firestore.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Event> events = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    Event event = document.toObject(Event.class);
                    if (event != null) {
                        event.setId(document.getId());
                        events.add(event);
                    }
                }
                eventAdapter.updateEventList(events);
            } else {
                Toast.makeText(EventActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
        // Add logout logic here (e.g., FirebaseAuth.getInstance().signOut())
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
