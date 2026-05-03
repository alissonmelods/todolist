package com.estudo.todolist.enums;

/**
 * Nível de prioridade de uma tarefa.
 *
 * <p>Persistido como String no banco (ex: {@code "ALTO"}) via {@code @Enumerated(EnumType.STRING)},
 * evitando dependência de índices ordinais em migrações futuras.</p>
 */
public enum Priority {

    /** Tarefa de baixa importância; pode ser feita quando houver disponibilidade. */
    BAIXO,

    /** Tarefa de importância moderada; deve ser feita em breve. */
    MEDIO,

    /** Tarefa urgente ou crítica; deve ser feita com máxima precedência. */
    ALTO
}
