package com.estudo.todolist.repositories;

import com.estudo.todolist.entities.TodoList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repositório de acesso a dados da entidade {@link TodoList}.
 *
 * <p>Herda operações CRUD completas de {@link JpaRepository} e suporte a
 * filtros dinâmicos de {@link JpaSpecificationExecutor}, utilizado pelo
 * {@code TodoListService#findAll} para compor predicados opcionais em tempo
 * de execução sem queries fixas.</p>
 */
@Repository
public interface TodoListRepository extends JpaRepository<TodoList, Long>, JpaSpecificationExecutor<TodoList> {
}
