package com.example.notes;

public class Task {
    private String id;
    private String title;
    private String description;
    private boolean completed;
    private long createdAt;
    private String imageUri;

    public Task() {
        this.id = String.valueOf(System.currentTimeMillis());
        this.createdAt = System.currentTimeMillis();
        this.completed = false;
    }

    public Task(String title, String description) {
        this();
        this.title = title;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) {
        if (imageUri != null && (imageUri.equals("null") || imageUri.isEmpty())) {
            this.imageUri = null;
        } else {
            this.imageUri = imageUri;
        }
    }

    public boolean hasImage() {
        return imageUri != null && !imageUri.isEmpty() && !"null".equals(imageUri);
    }
}