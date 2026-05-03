package com.estudo.todolist.dtos;

import com.estudo.todolist.entities.TodoList;
import com.estudo.todolist.enums.Category;
import com.estudo.todolist.enums.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de saída para respostas dos endpoints CRUD de tarefas.
 *
 * <p>Expõe todos os campos da entidade {@link TodoList}, incluindo os campos
 * gerenciados pelo sistema ({@code id}, {@code createdAt}, {@code updatedAt}).</p>
 *
 * @param id          Identificador único da tarefa.
 * @param createdAt   Timestamp de criação, preenchido automaticamente.
 * @param updatedAt   Timestamp da última atualização, preenchido automaticamente.
 * @param title       Título da tarefa.
 * @param description Detalhes adicionais da tarefa.
 * @param completed   Indica se a tarefa foi concluída.
 * @param deadline    Data-limite para conclusão.
 * @param priority    Nível de prioridade.
 * @param category    Categoria da tarefa.
 */
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
    /**
     * Converte uma entidade {@link TodoList} neste DTO.
     *
     * @param entity entidade persistida a ser convertida.
     * @return novo {@code TodoListResponseDTO} com os dados da entidade.
     */
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
