package com.estudo.todolist.enums;

/**
 * Categoria que classifica o contexto de uma tarefa.
 *
 * <p>Persistido como String no banco (ex: {@code "TRABALHO"}) via {@code @Enumerated(EnumType.STRING)},
 * evitando dependência de índices ordinais em migrações futuras.</p>
 */
public enum Category {

    /** Tarefas relacionadas ao ambiente profissional. */
    TRABALHO,

    /** Tarefas voltadas a aprendizado, cursos ou pesquisa. */
    ESTUDO,

    /** Tarefas de cunho pessoal ou doméstico. */
    PESSOAL
}
