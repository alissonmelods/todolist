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

/**
 * Camada de serviço responsável pela consulta ao histórico de auditoria das tarefas.
 *
 * <p>Utiliza a API do Hibernate Envers ({@link AuditReaderFactory}) para acessar a tabela
 * {@code todolist_audit}, que registra automaticamente cada operação de INSERT, UPDATE
 * e DELETE sobre a entidade {@link TodoList}.</p>
 *
 * <p>As consultas são marcadas com {@code readOnly = true} para evitar abertura de
 * transações de escrita desnecessárias, não impactando a performance das operações CRUD.</p>
 */
@Service
public class TodoListAuditService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Retorna o histórico de revisões de uma tarefa específica, ordenado da mais recente para a mais antiga.
     *
     * <p>Caso o ID não possua histórico (tarefa nunca existiu ou Envers ainda não registrou revisões),
     * retorna uma lista vazia sem lançar exceção.</p>
     *
     * @param id identificador da tarefa cujo histórico será consultado.
     * @return lista de {@link TodoListAuditDTO} com o estado da tarefa em cada revisão.
     */
    @Transactional(readOnly = true)
    public List<TodoListAuditDTO> findAuditByTaskId(Long id) {
        AuditReader reader = AuditReaderFactory.get(entityManager);

        // forRevisionsOfEntity(class, selectEntitiesOnly=false, selectDeletedEntities=true):
        // retorna Object[] com [entidade, DefaultRevisionEntity, RevisionType] para cada revisão,
        // incluindo as revisões de deleção (necessário para exibir operações DELETE no histórico).
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

    /**
     * Retorna o histórico de revisões de todas as tarefas, ordenado da mais recente para a mais antiga.
     *
     * @return lista de {@link TodoListAuditDTO} com todas as revisões registradas pelo Envers.
     */
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

    /**
     * Converte uma linha de resultado do Envers ({@code Object[]}) em {@link TodoListAuditDTO}.
     *
     * <p>O Envers retorna cada revisão como um array de três posições:
     * <ul>
     *   <li>[0] — snapshot da entidade {@link TodoList} no momento da revisão.</li>
     *   <li>[1] — {@link DefaultRevisionEntity} com {@code id} (número da revisão) e {@code revisionDate}.</li>
     *   <li>[2] — {@link RevisionType} indicando a operação: {@code ADD}, {@code MOD} ou {@code DEL}.</li>
     * </ul>
     * Com {@code store_data_at_delete=true}, o snapshot em [0] nunca é nulo, mesmo para deleções.</p>
     *
     * @param row array de três elementos retornado pelo {@code AuditQuery}.
     * @return DTO populado com os dados da revisão.
     */
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
                // getRevisionDate() retorna java.util.Date; converte para LocalDateTime usando o fuso local
                LocalDateTime.ofInstant(revision.getRevisionDate().toInstant(), ZoneId.systemDefault()),
                mapRevisionType(revisionType)
        );
    }

    /**
     * Traduz o {@link RevisionType} do Envers para uma string legível na API.
     *
     * <p>Internamente o Envers usa códigos numéricos (0, 1, 2); este método os expõe
     * como strings semânticas para facilitar o consumo por clientes da API.</p>
     *
     * @param type tipo de revisão retornado pelo Envers.
     * @return {@code "CREATE"}, {@code "UPDATE"} ou {@code "DELETE"}.
     */
    private String mapRevisionType(RevisionType type) {
        return switch (type) {
            case ADD -> "CREATE";
            case MOD -> "UPDATE";
            case DEL -> "DELETE";
        };
    }
}
