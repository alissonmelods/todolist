package com.estudo.todolist.dtos;

import com.estudo.todolist.enums.Category;
import com.estudo.todolist.enums.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de saída para os endpoints de auditoria.
 *
 * <p>Combina o estado completo da tarefa no momento da revisão com os
 * metadados gerados pelo Hibernate Envers: identificador da revisão,
 * data/hora em que ocorreu e o tipo da operação.</p>
 *
 * @param id           Identificador da tarefa auditada.
 * @param createdAt    Timestamp de criação original da tarefa.
 * @param updatedAt    Timestamp da última atualização no momento da revisão.
 * @param title        Título da tarefa no momento da revisão.
 * @param description  Descrição da tarefa no momento da revisão.
 * @param completed    Status de conclusão no momento da revisão.
 * @param deadline     Data-limite no momento da revisão.
 * @param priority     Prioridade no momento da revisão.
 * @param category     Categoria no momento da revisão.
 * @param revisionId   Número sequencial da revisão gerado pelo Envers.
 * @param revisionDate Timestamp em que a revisão foi registrada.
 * @param revisionType Tipo da operação: {@code "CREATE"}, {@code "UPDATE"} ou {@code "DELETE"}.
 */
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
