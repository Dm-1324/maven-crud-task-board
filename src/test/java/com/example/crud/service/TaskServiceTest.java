package com.example.crud.service;

import com.example.crud.model.Task;
import com.example.crud.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock TaskRepository repository;
    @InjectMocks TaskService service;

    @Test
    void createTaskWithBlankTitleThrows() {
        Task task = new Task("", "desc", "TODO");
        assertThrows(IllegalArgumentException.class, () -> service.create(task));
    }

    @Test
    void createTaskDefaultsInvalidStatusToTodo() {
        Task task = new Task("Write tests", "desc", "BOGUS_STATUS");
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(TaskService.TODO, service.create(task).getStatus());
    }

    @Test
    void createTaskKeepsValidStatus() {
        Task task = new Task("Ship app", "desc", TaskService.IN_PROGRESS);
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(TaskService.IN_PROGRESS, service.create(task).getStatus());
    }

    @Test
    void updateStatusWithInvalidValueThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(1L, "NOT_REAL"));
    }

    @Test
    void updateStatusOnMissingTaskThrows() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(99L, TaskService.DONE));
    }

    @Test
    void updateStatusChangesExistingTask() {
        Task task = new Task("Ship app", "desc", TaskService.TODO);
        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(TaskService.DONE, service.updateStatus(1L, TaskService.DONE).getStatus());
    }

    @Test
    void deleteMissingTaskThrows() {
        when(repository.existsById(10L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.delete(10L));
    }
}
