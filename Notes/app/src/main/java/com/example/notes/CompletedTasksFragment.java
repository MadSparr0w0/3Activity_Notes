package com.example.notes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class CompletedTasksFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private FloatingActionButton fabDelete;
    private LinearLayout selectionToolbar;
    private DataManager dataManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completed_tasks, container, false);

        dataManager = DataManager.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.completed_tasks_recycler_view);
        fabDelete = view.findViewById(R.id.fab_delete);
        selectionToolbar = view.findViewById(R.id.selection_toolbar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TaskAdapter(dataManager.getCompletedTasks(), this);
        recyclerView.setAdapter(adapter);

        fabDelete.setOnClickListener(v -> deleteSelectedTasks());

        return view;
    }

    @Override
    public void onTaskClick(int position) {
        if (adapter.isSelectionMode()) {
            adapter.toggleSelection(position);
            updateSelectionUI();
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
        } else {
            selectionToolbar.setVisibility(View.GONE);
        }
    }

    private void deleteSelectedTasks() {
        List<Integer> selectedPositions = adapter.getSelectedPositions();
        if (selectedPositions.isEmpty()) {
            Toast.makeText(getContext(), "Выберите задачи", Toast.LENGTH_SHORT).show();
            return;
        }

        dataManager.deleteCompletedTasks(selectedPositions);
        adapter.updateTasks(dataManager.getCompletedTasks());
        adapter.setSelectionMode(false);
        updateSelectionUI();

        Toast.makeText(getContext(), "Задачи удалены", Toast.LENGTH_SHORT).show();
    }
}