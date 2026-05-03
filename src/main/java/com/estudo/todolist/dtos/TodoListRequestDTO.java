package com.estudo.todolist.dtos;

import com.estudo.todolist.enums.Category;
import com.estudo.todolist.enums.Priority;

import java.time.LocalDate;

/**
 * DTO de entrada para criação e atualização de tarefas.
 *
 * <p>Expõe apenas os campos que o cliente pode fornecer, excluindo campos
 * gerenciados pelo sistema como {@code id}, {@code createdAt}, {@code updatedAt}
 * e {@code completed} (este último é alterado exclusivamente via {@code PATCH /done/{id}}).</p>
 *
 * @param title       Título curto da tarefa (obrigatório).
 * @param description Detalhes adicionais (opcional).
 * @param deadline    Data-limite para conclusão (opcional).
 * @param priority    Nível de prioridade (obrigatório).
 * @param category    Categoria da tarefa (obrigatório).
 */
public record TodoListRequestDTO(
        String title,
        String description,
        LocalDate deadline,
        Priority priority,
        Category category
) {}
