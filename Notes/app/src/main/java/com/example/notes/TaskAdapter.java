package com.example.notes;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskClickListener listener;
    private List<Integer> selectedPositions = new ArrayList<>();
    private boolean selectionMode = false;

    public interface OnTaskClickListener {
        void onTaskClick(int position);
        void onTaskLongClick(int position);
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;
        public ImageView taskImageView;
        public View itemContainer;

        public TaskViewHolder(View itemView, final OnTaskClickListener listener) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.task_title);
            descriptionTextView = itemView.findViewById(R.id.task_description);
            taskImageView = itemView.findViewById(R.id.task_image);
            itemContainer = itemView.findViewById(R.id.task_item_container);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTaskClick(position);
                    }
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTaskLongClick(position);
                        return true;
                    }
                }
                return false;
            });
        }
    }

    public TaskAdapter(List<Task> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.titleTextView.setText(task.getTitle());

        String description = task.getDescription();
        if (description != null && !description.isEmpty()) {
            String textOnly = description.replaceAll("<img[^>]*>", "[Изображение]");
            String plainText = android.text.Html.fromHtml(textOnly).toString();

            if (plainText.length() > 100) {
                plainText = plainText.substring(0, 100) + "...";
            }
            holder.descriptionTextView.setText(plainText);

            if (description.contains("<img")) {
                holder.taskImageView.setVisibility(View.VISIBLE);
                loadFirstImage(holder.taskImageView, description);
            } else {
                holder.taskImageView.setVisibility(View.GONE);
            }
        } else {
            holder.descriptionTextView.setText("");
            holder.taskImageView.setVisibility(View.GONE);
        }

        if (selectedPositions.contains(position)) {
            holder.itemContainer.setBackgroundResource(R.drawable.task_item_background_selected);
        } else {
            holder.itemContainer.setBackgroundResource(R.drawable.task_item_background);
        }
    }

    private void loadFirstImage(ImageView imageView, String html) {
        try {
            Pattern pattern = Pattern.compile("src=\"data:image/jpeg;base64,([^\"]+)\"");
            Matcher matcher = pattern.matcher(html);

            if (matcher.find()) {
                String base64 = matcher.group(1);
                byte[] decodedBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                com.bumptech.glide.Glide.with(imageView.getContext())
                        .load(bitmap)
                        .centerCrop()
                        .placeholder(R.drawable.ic_default_image)
                        .error(R.drawable.ic_default_image)
                        .into(imageView);
            }
        } catch (Exception e) {
            imageView.setVisibility(View.GONE);
        }
    }

    private String extractFirstImageBase64(String html) {
        int start = html.indexOf("base64,");
        if (start != -1) {
            start += 7;
            int end = html.indexOf("\"", start);
            if (end != -1) {
                return html.substring(start, end);
            }
        }
        return null;
    }

    private void loadBase64Image(ImageView imageView, String base64) {
        try {
            byte[] decodedBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            Glide.with(imageView.getContext())
                    .load(bitmap)
                    .centerCrop()
                    .placeholder(R.drawable.ic_default_image)
                    .error(R.drawable.ic_default_image)
                    .into(imageView);
        } catch (Exception e) {
            imageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTasks(List<Task> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove((Integer) position);
        } else {
            selectedPositions.add(position);
        }
        notifyItemChanged(position);
    }

    public void clearSelection() {
        selectedPositions.clear();
        selectionMode = false;
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedPositions() {
        return new ArrayList<>(selectedPositions);
    }

    public void setSelectionMode(boolean mode) {
        this.selectionMode = mode;
        if (!mode) {
            clearSelection();
        }
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }
}