package com.example.notes;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPrefManager {
    private static final String PREF_NAME = "task_manager_pref";
    private static final String KEY_TASKS = "tasks";
    private static final String KEY_COMPLETED_TASKS = "completed_tasks";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveTasks(List<Task> tasks) {
        String json = gson.toJson(tasks);
        sharedPreferences.edit().putString(KEY_TASKS, json).apply();
    }

    public List<Task> getTasks() {
        String json = sharedPreferences.getString(KEY_TASKS, null);
        if (json == null) {
            List<Task> defaultTasks = new ArrayList<>();
            defaultTasks.add(new Task("Купить продукты", "Молоко, хлеб, яйца, фрукты"));
            defaultTasks.add(new Task("Сделать домашку", "Математика и физика"));
            defaultTasks.add(new Task("Позвонить маме", "Обсудить планы на выходные"));
            return defaultTasks;
        }

        Type type = new TypeToken<List<Task>>() {}.getType();
        List<Task> tasks = gson.fromJson(json, type);

        if (tasks != null) {
            for (Task task : tasks) {
                if (task.getImageUri() != null &&
                        (task.getImageUri().equals("null") || task.getImageUri().isEmpty())) {
                    task.setImageUri(null);
                }
            }
        }

        return tasks != null ? tasks : new ArrayList<>();
    }

    public void saveCompletedTasks(List<Task> completedTasks) {
        String json = gson.toJson(completedTasks);
        sharedPreferences.edit().putString(KEY_COMPLETED_TASKS, json).apply();
    }

    public List<Task> getCompletedTasks() {
        String json = sharedPreferences.getString(KEY_COMPLETED_TASKS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Task>>() {}.getType();
        List<Task> tasks = gson.fromJson(json, type);

        if (tasks != null) {
            for (Task task : tasks) {
                if (task.getImageUri() != null &&
                        (task.getImageUri().equals("null") || task.getImageUri().isEmpty())) {
                    task.setImageUri(null);
                }
            }
        }

        return tasks != null ? tasks : new ArrayList<>();
    }

    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }
}