package com.example.myfinalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CabinetAdapter extends RecyclerView.Adapter<CabinetAdapter.CabinetViewHolder> {

    private List<Cabinet> cabinetList;

    public CabinetAdapter(List<Cabinet> cabinetList) {
        this.cabinetList = cabinetList;
    }

    @NonNull
    @Override
    public CabinetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cabinet, parent, false);
        return new CabinetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CabinetViewHolder holder, int position) {
        Cabinet cabinet = cabinetList.get(position);
        holder.txtCabinetName.setText(cabinet.getCabinetName());
        holder.txtVision.setText(cabinet.getVision());
        holder.txtMission.setText(cabinet.getMission());

        Glide.with(holder.itemView.getContext())
                .load(cabinet.getImageUrl())
                .into(holder.imgCabinet);

        holder.btnDelete.setOnClickListener(v -> {
            deleteCabinet(holder, cabinet);
        });
    }

    @Override
    public int getItemCount() {
        return cabinetList.size();
    }

    private void deleteCabinet(CabinetViewHolder holder, Cabinet cabinet) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cabinet")
                .document(cabinet.getDocumentId()) // Gunakan documentId untuk menghapus
                .delete()
                .addOnSuccessListener(aVoid -> {
                    int position = holder.getAdapterPosition();
                    cabinetList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(holder.itemView.getContext(), "Cabinet deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(holder.itemView.getContext(), "Failed to delete cabinet: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    public static class CabinetViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCabinet;
        TextView txtCabinetName, txtVision, txtMission;
        Button btnDelete;

        public CabinetViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCabinet = itemView.findViewById(R.id.img_cabinet);
            txtCabinetName = itemView.findViewById(R.id.txt_cabinet_name);
            txtVision = itemView.findViewById(R.id.txt_vision);
            txtMission = itemView.findViewById(R.id.txt_mission);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
