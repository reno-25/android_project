package com.example.myfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoteActivity extends AppCompatActivity {

    private TextView voteTitleTextView;
    private RecyclerView optionsRecyclerView;
    private Button submitVoteButton;
    private Button createVoteButton;

    private FirebaseFirestore firestore;
    private String voteId;

    private List<Vote> voteList;
    private VoteAdapter voteAdapter;
    private List<String> optionsText;
    private List<String> optionsImages;
    private VoteAdapter.OptionsAdapter optionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote); // Ensure it uses the updated layout

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize views from the layout
        voteTitleTextView = findViewById(R.id.voteTitleTextView);
        optionsRecyclerView = findViewById(R.id.optionsRecyclerView);
        submitVoteButton = findViewById(R.id.submitVoteButton);
        createVoteButton = findViewById(R.id.createVoteButton);

        // Initialize lists
        optionsText = new ArrayList<>();
        optionsImages = new ArrayList<>();
        voteList = new ArrayList<>();

        // Set up RecyclerView for vote options
        optionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch votes from Firestore
        fetchVotes();

        // Submit the vote when the button is clicked
        submitVoteButton.setOnClickListener(v -> {
            int selectedPosition = optionsAdapter.getSelectedPosition();
            if (selectedPosition != -1) {
                castVote(selectedPosition);  // Proceed with casting the vote
            } else {
                Toast.makeText(VoteActivity.this, "Please select an option", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the Create Vote button (navigate to AddVoteActivity)
        createVoteButton.setOnClickListener(v -> {
            // Create an Intent to open AddVoteActivity
            Intent intent = new Intent(VoteActivity.this, CreateVoteActivity.class);
            startActivity(intent);
        });
    }

    // Fetch the list of votes from Firestore
    private void fetchVotes() {
        firestore.collection("votes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String voteTitle = document.getString("voteTitle");
                            List<String> optionsText = (List<String>) document.get("optionsText");
                            List<String> optionsImages = (List<String>) document.get("optionsImages");

                            if (voteTitle != null && optionsText != null && optionsImages != null) {
                                voteList.add(new Vote(document.getId(), voteTitle, optionsText, optionsImages));
                            }
                        }

                        // Set the first vote as the selected one (or allow user to choose a vote)
                        if (!voteList.isEmpty()) {
                            displayVoteDetails(0);  // Assuming you want to show the first vote by default
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(VoteActivity.this, "Error fetching votes", Toast.LENGTH_SHORT).show();
                });
    }

    // Display vote details after clicking a vote
    private void displayVoteDetails(int position) {
        Vote selectedVote = voteList.get(position);
        voteId = selectedVote.getVoteId();
        String voteTitle = selectedVote.getVoteTitle();
        List<String> optionsTextList = selectedVote.getOptionsText();
        List<String> optionsImagesList = selectedVote.getOptionsImages();

        // Set vote title and options
        voteTitleTextView.setText(voteTitle);

        // Filter out null or empty options before updating the adapter
        List<String> validOptionsText = new ArrayList<>();
        List<String> validOptionsImages = new ArrayList<>();
        for (int i = 0; i < optionsTextList.size(); i++) {
            if (optionsTextList.get(i) != null && !optionsTextList.get(i).isEmpty()) {
                validOptionsText.add(optionsTextList.get(i));
                validOptionsImages.add(optionsImagesList != null && optionsImagesList.size() > i ? optionsImagesList.get(i) : null);
            }
        }

        // Initialize the OptionsAdapter with the filtered options
        optionsAdapter = new VoteAdapter.OptionsAdapter(validOptionsText, validOptionsImages);
        optionsRecyclerView.setAdapter(optionsAdapter);

        // Enable the submit vote button and make it visible
        submitVoteButton.setEnabled(true);  // Ensure the button is enabled
        submitVoteButton.setVisibility(View.VISIBLE);  // Ensure the button is visible
    }

    // Cast the vote by updating the Firestore document with the selected option
    private void castVote(int selectedPosition) {
        DocumentReference voteRef = firestore.collection("votes").document(voteId);

        // Create a map to store the vote data (increment the vote count for the selected option)
        Map<String, Object> voteData = new HashMap<>();
        voteData.put("votes." + selectedPosition, FieldValue.increment(1));  // Increment the count for the selected option

        // Update the vote data in Firestore
        voteRef.update(voteData).addOnSuccessListener(aVoid -> {
            Toast.makeText(VoteActivity.this, "Vote casted successfully", Toast.LENGTH_SHORT).show();
            // Optionally, reset to the vote options view or close the activity
        }).addOnFailureListener(e -> {
            Toast.makeText(VoteActivity.this, "Error casting vote", Toast.LENGTH_SHORT).show();
        });
    }
}
