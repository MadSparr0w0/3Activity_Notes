package com.example.notes;

import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static DataManager instance;
    private List<Task> tasks;
    private List<Task> completedTasks;
    private SharedPrefManager sharedPrefManager;

    private DataManager(Context context) {
        sharedPrefManager = new SharedPrefManager(context);
        tasks = sharedPrefManager.getTasks();
        completedTasks = sharedPrefManager.getCompletedTasks();
    }

    public static DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public List<Task> getCompletedTasks() {
        return completedTasks;
    }

    public void addTask(Task task) {
        tasks.add(0, task);
        sharedPrefManager.saveTasks(tasks);
    }

    public void updateTask(int position, Task task) {
        tasks.set(position, task);
        sharedPrefManager.saveTasks(tasks);
    }

    public void deleteTasks(List<Integer> positions) {
        List<Integer> sortedPositions = new ArrayList<>(positions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sortedPositions.sort((a, b) -> b - a);
        }

        for (int position : sortedPositions) {
            tasks.remove(position);
        }
        sharedPrefManager.saveTasks(tasks);
    }

    public void completeTasks(List<Integer> positions) {
        List<Integer> sortedPositions = new ArrayList<>(positions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sortedPositions.sort((a, b) -> b - a);
        }

        for (int position : sortedPositions) {
            Task task = tasks.get(position);
            task.setCompleted(true);
            completedTasks.add(0, task);
            tasks.remove(position);
        }
        sharedPrefManager.saveTasks(tasks);
        sharedPrefManager.saveCompletedTasks(completedTasks);
    }

    public void deleteCompletedTasks(List<Integer> positions) {
        List<Integer> sortedPositions = new ArrayList<>(positions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sortedPositions.sort((a, b) -> b - a);
        }

        for (int position : sortedPositions) {
            completedTasks.remove(position);
        }
        sharedPrefManager.saveCompletedTasks(completedTasks);
    }
}