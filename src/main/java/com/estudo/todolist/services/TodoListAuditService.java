package com.estudo.todolist.services;

import com.estudo.todolist.dtos.TodoListAuditDTO;
import com.estudo.todolist.entities.TodoList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class TodoListAuditService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<TodoListAuditDTO> findAuditByTaskId(Long id) {
        AuditReader reader = AuditReaderFactory.get(entityManager);

        @SuppressWarnings("unchecked")
        List<Object[]> results = reader.createQuery()
                .forRevisionsOfEntity(TodoList.class, false, true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionProperty("timestamp").desc())
                .getResultList();

        return results.stream()
                .map(this::toAuditDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TodoListAuditDTO> findAllAudits() {
        AuditReader reader = AuditReaderFactory.get(entityManager);

        @SuppressWarnings("unchecked")
        List<Object[]> results = reader.createQuery()
                .forRevisionsOfEntity(TodoList.class, false, true)
                .addOrder(AuditEntity.revisionProperty("timestamp").desc())
                .getResultList();

        return results.stream()
                .map(this::toAuditDTO)
                .toList();
    }

    private TodoListAuditDTO toAuditDTO(Object[] row) {
        TodoList entity = (TodoList) row[0];
        DefaultRevisionEntity revision = (DefaultRevisionEntity) row[1];
        RevisionType revisionType = (RevisionType) row[2];

        return new TodoListAuditDTO(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getTitle(),
                entity.getDescription(),
                entity.isCompleted(),
                entity.getDeadline(),
                entity.getPriority(),
                entity.getCategory(),
                (long) revision.getId(),
                LocalDateTime.ofInstant(revision.getRevisionDate().toInstant(), ZoneId.systemDefault()),
                mapRevisionType(revisionType)
        );
    }

    private String mapRevisionType(RevisionType type) {
        return switch (type) {
            case ADD -> "CREATE";
            case MOD -> "UPDATE";
            case DEL -> "DELETE";
        };
    }
}
