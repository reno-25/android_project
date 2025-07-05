    package com.example.myfinalproject;

    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.RadioButton;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.squareup.picasso.Picasso;

    import java.util.ArrayList;
    import java.util.List;

    public class VoteAdapter extends RecyclerView.Adapter<VoteAdapter.VoteViewHolder> {
        private List<Vote> voteList;
        private OnItemClickListener onItemClickListener;

        public VoteAdapter(List<Vote> voteList) {
            this.voteList = voteList;
        }

        @NonNull
        @Override
        public VoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate the layout for the vote item
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vote_item_layout, parent, false);
            return new VoteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VoteViewHolder holder, int position) {
            // Bind data to the ViewHolder
            holder.bind(voteList.get(position));
        }

        @Override
        public int getItemCount() {
            return voteList.size();
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.onItemClickListener = listener;
        }

        public class VoteViewHolder extends RecyclerView.ViewHolder {
            private TextView voteTitleTextView;
            private RecyclerView optionsRecyclerView;
            private OptionsAdapter optionsAdapter;

            public VoteViewHolder(View itemView) {
                super(itemView);
                // Initialize views
                voteTitleTextView = itemView.findViewById(R.id.voteTitleTextView);
                optionsRecyclerView = itemView.findViewById(R.id.optionsRecyclerView);

                // Set up the inner RecyclerView for options, only once
                optionsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            }

            public void bind(Vote vote) {
                // Set the vote title
                voteTitleTextView.setText(vote.getVoteTitle());

                // Filter out null or empty options before passing to the adapter
                List<String> validOptionsText = new ArrayList<>();
                List<String> validOptionsImages = new ArrayList<>();

                for (int i = 0; i < vote.getOptionsText().size(); i++) {
                    if (vote.getOptionsText().get(i) != null && !vote.getOptionsText().get(i).isEmpty()) {
                        validOptionsText.add(vote.getOptionsText().get(i));
                        validOptionsImages.add(vote.getOptionsImages().get(i));
                    }
                }

                // Initialize or update the OptionsAdapter with the filtered options
                OptionsAdapter optionsAdapter = new OptionsAdapter(validOptionsText, validOptionsImages);
                optionsRecyclerView.setAdapter(optionsAdapter);

                // Handle item click in the ViewHolder
                itemView.setOnClickListener(v -> {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(getAdapterPosition());
                    }
                });
            }
        }

        public interface OnItemClickListener {
            void onItemClick(int position);
        }

        public static class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.OptionsViewHolder> {
            private List<String> optionsText;
            private List<String> optionsImages;
            private int selectedPosition = -1;  // Track the selected option position

            // Constructor for OptionsAdapter
            public OptionsAdapter(List<String> optionsText, List<String> optionsImages) {
                this.optionsText = optionsText;
                this.optionsImages = optionsImages;
            }

            @NonNull
            @Override
            public OptionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Inflate the layout for options using the new option_item.xml
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.options_item, parent, false);
                return new OptionsViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull OptionsViewHolder holder, int position) {
                // Bind data to the ViewHolder
                String optionText = optionsText.get(position);
                String optionImage = optionsImages.get(position);

                // Ensure the image and text are valid
                holder.bind(optionText, optionImage, position);
            }

            @Override
            public int getItemCount() {
                return optionsText.size();
            }

            public int getSelectedPosition() {
                return selectedPosition;
            }

            // ViewHolder for each option
            public class OptionsViewHolder extends RecyclerView.ViewHolder {
                private RadioButton optionRadioButton;
                private TextView optionTextView;
                private ImageView optionImageView;

                public OptionsViewHolder(View itemView) {
                    super(itemView);
                    // Initialize the views
                    optionRadioButton = itemView.findViewById(R.id.radioButton);
                    optionTextView = itemView.findViewById(R.id.optionTextView);
                    optionImageView = itemView.findViewById(R.id.optionImageView);
                }

                public void bind(String optionText, String optionImage, int position) {
                    // Set the text for the option
                    optionTextView.setText(optionText);

                    // Check if the image URL is valid
                    if (optionImage != null && !optionImage.isEmpty()) {
                        // Load the image into the ImageView using Picasso (or Glide)
                        Picasso.get().load(optionImage).into(optionImageView);
                    } else {
                        // Set a placeholder or hide the ImageView if no valid image is available
                        optionImageView.setImageResource(R.drawable.placeholder_image);  // Example placeholder image
                    }

                    // If this option is selected, check the RadioButton
                    optionRadioButton.setChecked(position == selectedPosition);

                    // Set up the RadioButton click listener
                    optionRadioButton.setOnClickListener(v -> {
                        int previousSelectedPosition = selectedPosition;
                        selectedPosition = position;  // Update the selected position

                        // Notify only the previous and current selected items
                        notifyItemChanged(previousSelectedPosition);
                        notifyItemChanged(selectedPosition);
                    });
                }
            }
        }
    }