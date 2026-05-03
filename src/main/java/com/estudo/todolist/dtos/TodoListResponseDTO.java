package com.estudo.todolist.dtos;

import com.estudo.todolist.entities.TodoList;
import com.estudo.todolist.enums.Category;
import com.estudo.todolist.enums.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TodoListResponseDTO(
        Long id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String title,
        String description,
        boolean completed,
        LocalDate deadline,
        Priority priority,
        Category category
) {
    public static TodoListResponseDTO from(TodoList entity) {
        return new TodoListResponseDTO(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getTitle(),
                entity.getDescription(),
                entity.isCompleted(),
                entity.getDeadline(),
                entity.getPriority(),
                entity.getCategory()
        );
    }
}
