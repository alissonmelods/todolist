package com.estudo.todolist.dtos;

import com.estudo.todolist.enums.Category;
import com.estudo.todolist.enums.Priority;

import java.time.LocalDate;

public record TodoListRequestDTO(
        String title,
        String description,
        LocalDate deadline,
        Priority priority,
        Category category
) {}
