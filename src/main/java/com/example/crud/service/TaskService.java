package com.example.crud.service;

import com.example.crud.model.Task;
import com.example.crud.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class TaskService {
    public static final String TODO = "TODO";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String DONE = "DONE";
    private static final Set<String> VALID_STATUSES = Set.of(TODO, IN_PROGRESS, DONE);

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> getAll() {
        return repository.findAll();
    }

    public Task create(Task task) {
        if (task == null || task.getTitle() == null || task.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (task.getStatus() == null || !VALID_STATUSES.contains(task.getStatus())) {
            task.setStatus(TODO);
        }
        return repository.save(task);
    }

    public Task updateStatus(Long id, String newStatus) {
        if (newStatus == null || !VALID_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }
        Task task = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
        task.setStatus(newStatus);
        return repository.save(task);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Task not found: " + id);
        }
        repository.deleteById(id);
    }
}
