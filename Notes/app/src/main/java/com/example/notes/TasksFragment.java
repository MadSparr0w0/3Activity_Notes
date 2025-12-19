package com.example.notes;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private static final int PICK_IMAGE_REQUEST = 1;

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private FloatingActionButton fabAddTask, fabComplete, fabDelete;
    private android.widget.LinearLayout selectionToolbar;
    private DataManager dataManager;
    private int editingPosition = -1;
    private AlertDialog currentDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        dataManager = DataManager.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.tasks_recycler_view);
        fabAddTask = view.findViewById(R.id.fab_add_task);
        fabComplete = view.findViewById(R.id.fab_complete);
        fabDelete = view.findViewById(R.id.fab_delete);
        selectionToolbar = view.findViewById(R.id.selection_toolbar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TaskAdapter(dataManager.getTasks(), this);
        recyclerView.setAdapter(adapter);

        fabAddTask.setOnClickListener(v -> showAddTaskDialog());
        fabComplete.setOnClickListener(v -> completeSelectedTasks());
        fabDelete.setOnClickListener(v -> deleteSelectedTasks());

        return view;
    }

    private void showAddTaskDialog() {
        if (!adapter.isSelectionMode()) {
            editingPosition = -1;
            showTaskDialog(null);
        }
    }

    private void showEditTaskDialog(int position) {
        if (!adapter.isSelectionMode()) {
            editingPosition = position;
            Task task = dataManager.getTasks().get(position);
            showTaskDialog(task);
        }
    }

    private void showTaskDialog(Task existingTask) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_task, null);

        EditText titleEditText = dialogView.findViewById(R.id.edit_text_title);
        ImageEditText descriptionEditText = dialogView.findViewById(R.id.edit_text_description); // ИСПРАВЛЕНО ЗДЕСЬ!
        Button btnAddImageInline = dialogView.findViewById(R.id.btn_add_image_inline);

        if (existingTask != null) {
            titleEditText.setText(existingTask.getTitle());

            if (existingTask.getDescription() != null && existingTask.getDescription().contains("<img")) {
                descriptionEditText.setHtmlContent(existingTask.getDescription());
            } else {
                descriptionEditText.setText(existingTask.getDescription());
            }
        }

        btnAddImageInline.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, "Выберите изображение для вставки в текст"), PICK_IMAGE_REQUEST);
        });

        builder.setView(dialogView)
                .setTitle(existingTask == null ? "Добавить задачу" : "Редактировать задачу")
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String title = titleEditText.getText().toString().trim();
                    String description = descriptionEditText.getHtmlContent(); ////////2222212122121

                    if (title.isEmpty() && description.isEmpty()) {
                        title = existingTask == null ? "Новая задача" : "Задача без названия";
                    }

                    if (existingTask == null) {
                        Task newTask = new Task(title, description);
                        dataManager.addTask(newTask);
                        adapter.updateTasks(dataManager.getTasks());
                        recyclerView.scrollToPosition(0);
                        Toast.makeText(getContext(), "Задача добавлена", Toast.LENGTH_SHORT).show();
                    } else {
                        Task updatedTask = dataManager.getTasks().get(editingPosition);
                        updatedTask.setTitle(title);
                        updatedTask.setDescription(description);
                        dataManager.updateTask(editingPosition, updatedTask);
                        adapter.updateTasks(dataManager.getTasks());
                        Toast.makeText(getContext(), "Задача обновлена", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null);

        currentDialog = builder.create();

        Window window = currentDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.85);
            window.setAttributes(layoutParams);
        }

        currentDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            if (currentDialog != null && currentDialog.isShowing()) {
                View dialogView = currentDialog.findViewById(R.id.edit_text_description);
                if (dialogView instanceof ImageEditText) { // ИСПРАВЛЕНО ЗДЕСЬ!
                    ImageEditText editText = (ImageEditText) dialogView; // ИСПРАВЛЕНО ЗДЕСЬ!

                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            editText.insertImage(imageUri);
                        }
                        Toast.makeText(getContext(), "Вставлено " + count + " изображений", Toast.LENGTH_SHORT).show();
                    } else if (data.getData() != null) {
                        Uri imageUri = data.getData();
                        editText.insertImage(imageUri);
                        Toast.makeText(getContext(), "Изображение вставлено", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onTaskClick(int position) {
        if (adapter.isSelectionMode()) {
            adapter.toggleSelection(position);
            updateSelectionUI();
        } else {
            showEditTaskDialog(position);
        }
    }

    @Override
    public void onTaskLongClick(int position) {
        adapter.setSelectionMode(true);
        adapter.toggleSelection(position);
        updateSelectionUI();
    }

    private void updateSelectionUI() {
        if (adapter.isSelectionMode()) {
            selectionToolbar.setVisibility(View.VISIBLE);
            fabAddTask.setVisibility(View.GONE);
        } else {
            selectionToolbar.setVisibility(View.GONE);
            fabAddTask.setVisibility(View.VISIBLE);
        }
    }

    private void completeSelectedTasks() {
        List<Integer> selectedPositions = adapter.getSelectedPositions();
        if (selectedPositions.isEmpty()) {
            Toast.makeText(getContext(), "Выберите задачи", Toast.LENGTH_SHORT).show();
            return;
        }

        dataManager.completeTasks(selectedPositions);
        adapter.updateTasks(dataManager.getTasks());
        adapter.setSelectionMode(false);
        updateSelectionUI();

        Toast.makeText(getContext(), "Задачи перенесены в выполненные", Toast.LENGTH_SHORT).show();
    }

    private void deleteSelectedTasks() {
        List<Integer> selectedPositions = adapter.getSelectedPositions();
        if (selectedPositions.isEmpty()) {
            Toast.makeText(getContext(), "Выберите задачи", Toast.LENGTH_SHORT).show();
            return;
        }

        dataManager.deleteTasks(selectedPositions);
        adapter.updateTasks(dataManager.getTasks());
        adapter.setSelectionMode(false);
        updateSelectionUI();

        Toast.makeText(getContext(), "Задачи удалены", Toast.LENGTH_SHORT).show();
    }
}