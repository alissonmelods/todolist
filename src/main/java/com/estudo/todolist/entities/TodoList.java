package com.estudo.todolist.entities;

import com.estudo.todolist.enums.Category;
import com.estudo.todolist.enums.Priority;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade JPA que representa uma tarefa na aplicação.
 *
 * <p>Mapeada para a tabela {@code todolist} no PostgreSQL. Timestamps de criação
 * e atualização são gerenciados automaticamente pelo Spring Data Auditing via
 * {@link AuditingEntityListener}.</p>
 *
 * <p>{@code @Audited} instrui o Hibernate Envers a registrar cada INSERT, UPDATE
 * e DELETE na tabela {@code todolist_audit}, preservando o histórico completo
 * de alterações. O comportamento {@code store_data_at_delete=true} garante que
 * o último estado do registro seja salvo mesmo em deleções.</p>
 */
@Audited
@Entity
@Table(name = "todolist")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoList {

    /**
     * Identificador único gerado pela sequence {@code todolist_id_seq} do PostgreSQL.
     * {@code allocationSize = 1} garante incremento unitário, sem gaps desnecessários.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "todolist_seq")
    @SequenceGenerator(name = "todolist_seq", sequenceName = "todolist_id_seq", allocationSize = 1)
    private Long id;

    /** Preenchido automaticamente pelo Spring Data no primeiro {@code save()}; nunca atualizado após isso. */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Atualizado automaticamente pelo Spring Data a cada {@code save()} subsequente. */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Título curto e descritivo da tarefa. Limitado a 100 caracteres. */
    @Column(nullable = false, length = 100)
    private String title;

    /** Detalhes adicionais da tarefa. Mapeado como TEXT no PostgreSQL, sem limite de tamanho. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Indica se a tarefa foi concluída. Inicializado como {@code false} na criação. */
    @Column(nullable = false)
    private boolean completed;

    /** Data-limite para conclusão da tarefa. Campo opcional. */
    @Column(name = "deadline")
    private LocalDate deadline;

    /** Nível de prioridade. Persistido como String para legibilidade no banco. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    /** Categoria que classifica o contexto da tarefa. Persistido como String para legibilidade no banco. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
}
