package com.example.myfinalproject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private Context context;

    public EventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    public void updateEventList(List<Event> events) {
        this.eventList = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        // Bind event details to views
        holder.eventName.setText(event.getName());
        holder.eventDate.setText(event.getDate());
        holder.eventDescription.setText(event.getDescription());

        // Load image into RecyclerView (using loadImageFromFirebase)
        loadImageFromFirebase(event.getImageUrl(), holder.eventImage);

        // Handle "Join Event" button click
        holder.joinEventButton.setOnClickListener(v -> {
            joinEvent(event.getId(), holder);  // Join the event and change button
        });

        // Handle "Show QR Code" button click
        holder.showQrCodeButton.setOnClickListener(v -> {
            showQrCode(event);  // Show QR Code dialog
        });

        // Handle image click to open full-image dialog
        holder.eventImage.setOnClickListener(v -> showFullImageDialog(event.getImageUrl()));

        // Handle "More Options" button click using PopupMenu
        holder.moreOptionsButton.setOnClickListener(v -> showPopupMenu(v, event, position));

        // Check if the user has already joined the event
        List<String> joins = event.getJoins();
        if (joins != null && joins.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            holder.joinEventButton.setVisibility(View.GONE);  // Hide "Join Event" if user has joined
            holder.showQrCodeButton.setVisibility(View.VISIBLE);  // Show "Show QR Code" button
        } else {
            holder.joinEventButton.setVisibility(View.VISIBLE);  // Show "Join Event" if user hasn't joined
            holder.showQrCodeButton.setVisibility(View.GONE);  // Hide "Show QR Code" button
        }
    }

    private void showPopupMenu(View view, Event event, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.event_options_menu); // Use your menu XML file

        // Handle the menu item clicks directly
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_e) {
                editEvent(event);  // Edit event
                return true;
            } else if (menuItem.getItemId() == R.id.menu_d) {
                deleteEvent(event.getId(), position);  // Delete event
                return true;
            }
            return false;
        });

        popupMenu.show(); // Show the menu
    }

    private void editEvent(Event event) {
        // Launch EditEventActivity to edit the event
        Intent intent = new Intent(context, EditEventActivity.class);
        intent.putExtra("eventId", event.getId());
        intent.putExtra("eventName", event.getName());
        intent.putExtra("eventDate", event.getDate());
        intent.putExtra("eventDescription", event.getDescription());
        context.startActivity(intent);
    }

    private void deleteEvent(String eventId, int position) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("events").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove the event from the list and notify the adapter
                    eventList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void joinEvent(String eventId, EventViewHolder holder) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        String userUid = auth.getCurrentUser().getUid();

        firestore.collection("events").document(eventId)
                .update("joins", FieldValue.arrayUnion(userUid))
                .addOnSuccessListener(aVoid -> {
                    // Change the button to "Show QR Code" after joining
                    holder.joinEventButton.setVisibility(View.GONE);
                    holder.showQrCodeButton.setVisibility(View.VISIBLE);
                    Toast.makeText(context, "Thank you for joining the event!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to join event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showQrCode(Event event) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserUid = auth.getCurrentUser().getUid();
        String currentUserName = auth.getCurrentUser().getDisplayName();
        String currentUserEmail = auth.getCurrentUser().getEmail(); // Fetch email from Firebase

        if (currentUserName == null || currentUserName.isEmpty()) {
            currentUserName = "Unknown User"; // Default value if username is null or empty
        }

        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            currentUserEmail = "Unknown Email"; // Default value if email is null or empty
        }

        // Get the event details
        String eventId = event.getId();

        // Generate the data for the QR code: userId + eventId + email
        String qrData = currentUserUid + ":" + eventId + ":" + currentUserEmail ; // Format: userId:eventId:email

        try {
            // Generate the QR code as a Bitmap
            Bitmap qrCodeBitmap = generateQRCode(qrData);

            // Show the QR code in a dialog
            Dialog qrDialog = new Dialog(context);
            qrDialog.setContentView(R.layout.dialog_qr_code); // Layout with an ImageView for QR code

            ImageView qrImageView = qrDialog.findViewById(R.id.qrImageView);
            qrImageView.setImageBitmap(qrCodeBitmap);

            qrDialog.show();

            qrImageView.setOnClickListener(v -> qrDialog.dismiss()); // Dismiss the dialog on click
        } catch (Exception e) {
            Toast.makeText(context, "Error generating QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private Bitmap generateQRCode(String data) throws Exception {
        // Generate the QR code using ZXing's MultiFormatWriter
        BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 512, 512);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        // Fill in the pixels for the QR code
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bitmap;
    }


    private void loadImageFromFirebase(String imageUrl, ImageView imageView) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

        storageReference.getBytes(1024 * 1024) // 1MB max size
                .addOnSuccessListener(bytes -> {
                    // Convert byte array to Bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imageView.setImageBitmap(bitmap);
                })
                .addOnFailureListener(exception -> {
                    // Handle failure
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show();
                });
    }

    private void showFullImageDialog(String imageUrl) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_full_image); // Use your layout file for dialog

        ImageView fullImageView = dialog.findViewById(R.id.dialogImageView);

        loadImageFromFirebase(imageUrl, fullImageView);

        dialog.show();

        fullImageView.setOnClickListener(v -> dialog.dismiss());
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView eventName, eventDate, eventDescription;
        ImageView eventImage, moreOptionsButton;
        Button joinEventButton, showQrCodeButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            eventName = itemView.findViewById(R.id.eventName);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventDescription = itemView.findViewById(R.id.eventDescription);
            eventImage = itemView.findViewById(R.id.eventImage);
            joinEventButton = itemView.findViewById(R.id.joinEventButton);
            moreOptionsButton = itemView.findViewById(R.id.moreOptionsButton);
            showQrCodeButton = itemView.findViewById(R.id.showQrCodeButton);
        }
    }
}