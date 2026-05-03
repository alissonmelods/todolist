package com.estudo.todolist.dtos;

import com.estudo.todolist.enums.Category;
import com.estudo.todolist.enums.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TodoListAuditDTO(
        Long id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String title,
        String description,
        boolean completed,
        LocalDate deadline,
        Priority priority,
        Category category,
        Long revisionId,
        LocalDateTime revisionDate,
        String revisionType
) {}
