package com.estudo.todolist.services;

import com.estudo.todolist.dtos.TodoListRequestDTO;
import com.estudo.todolist.dtos.TodoListResponseDTO;
import com.estudo.todolist.entities.TodoList;
import com.estudo.todolist.enums.Category;
import com.estudo.todolist.enums.Priority;
import com.estudo.todolist.repositories.TodoListRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Camada de serviço responsável pela lógica de negócio das operações CRUD de tarefas.
 *
 * <p>Toda interação com o banco de dados passa por esta classe, que garante:
 * <ul>
 *   <li>Delimitação correta de transações ({@code @Transactional}).</li>
 *   <li>Isolamento entre a camada HTTP (controller/DTO) e o modelo de domínio (entidade).</li>
 *   <li>Retorno de {@code 404 Not Found} padronizado quando um ID inexistente é referenciado.</li>
 * </ul>
 * </p>
 */
@Service
public class TodoListService {

    private final TodoListRepository repository;

    public TodoListService(TodoListRepository repository) {
        this.repository = repository;
    }

    /**
     * Cria uma nova tarefa com status inicial {@code completed = false}.
     *
     * @param dto dados fornecidos pelo cliente para a nova tarefa.
     * @return DTO com os dados persistidos, incluindo {@code id} e timestamps gerados.
     */
    @Transactional
    public TodoListResponseDTO create(TodoListRequestDTO dto) {
        TodoList entity = TodoList.builder()
                .title(dto.title())
                .description(dto.description())
                .deadline(dto.deadline())
                .priority(dto.priority())
                .category(dto.category())
                .completed(false)
                .build();
        return TodoListResponseDTO.from(repository.save(entity));
    }

    /**
     * Lista tarefas com filtros opcionais combinados por {@code AND}.
     *
     * <p>Todos os parâmetros são opcionais. Quando {@code null}, o filtro correspondente
     * é ignorado. Filtros preenchidos são combinados para restringir o resultado.</p>
     *
     * @param completed      filtra por status de conclusão.
     * @param createdAtStart início do intervalo de data de criação (inclusivo).
     * @param createdAtEnd   fim do intervalo de data de criação (inclusivo, até 23:59:59).
     * @param deadline       filtra por data-limite exata.
     * @param priority       filtra por nível de prioridade.
     * @param category       filtra por categoria.
     * @param search         busca parcial, sem distinção de maiúsculas, no {@code title} ou {@code description}.
     * @return lista de tarefas que satisfazem todos os filtros fornecidos.
     */
    @Transactional(readOnly = true)
    public List<TodoListResponseDTO> findAll(Boolean completed, LocalDate createdAtStart, LocalDate createdAtEnd,
                                             LocalDate deadline, Priority priority, Category category, String search) {
        return repository.findAll(buildFilter(completed, createdAtStart, createdAtEnd, deadline, priority, category, search))
                .stream()
                .map(TodoListResponseDTO::from)
                .toList();
    }

    /**
     * Busca uma tarefa pelo seu identificador.
     *
     * @param id identificador da tarefa.
     * @return DTO com os dados da tarefa encontrada.
     * @throws ResponseStatusException {@code 404 Not Found} se o ID não existir.
     */
    @Transactional(readOnly = true)
    public TodoListResponseDTO findById(Long id) {
        return repository.findById(id)
                .map(TodoListResponseDTO::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with id: " + id));
    }

    /**
     * Substitui os dados editáveis de uma tarefa existente.
     *
     * <p>Os campos {@code completed}, {@code createdAt} e {@code id} não são alterados
     * por este método. Para alternar o status de conclusão, use {@link #toggleCompleted}.</p>
     *
     * @param id  identificador da tarefa a ser atualizada.
     * @param dto novos dados a serem aplicados.
     * @return DTO com o estado atualizado da tarefa.
     * @throws ResponseStatusException {@code 404 Not Found} se o ID não existir.
     */
    @Transactional
    public TodoListResponseDTO update(Long id, TodoListRequestDTO dto) {
        TodoList entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with id: " + id));
        entity.setTitle(dto.title());
        entity.setDescription(dto.description());
        entity.setDeadline(dto.deadline());
        entity.setPriority(dto.priority());
        entity.setCategory(dto.category());
        return TodoListResponseDTO.from(repository.save(entity));
    }

    /**
     * Alterna o status de conclusão de uma tarefa ({@code true} → {@code false} e vice-versa).
     *
     * <p>Operação idempotente quando chamada duas vezes consecutivas, retornando ao estado original.</p>
     *
     * @param id identificador da tarefa.
     * @return DTO com o novo status de {@code completed}.
     * @throws ResponseStatusException {@code 404 Not Found} se o ID não existir.
     */
    @Transactional
    public TodoListResponseDTO toggleCompleted(Long id) {
        TodoList entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with id: " + id));
        entity.setCompleted(!entity.isCompleted());
        return TodoListResponseDTO.from(repository.save(entity));
    }

    /**
     * Remove permanentemente uma tarefa pelo seu identificador.
     *
     * @param id identificador da tarefa a ser removida.
     * @throws ResponseStatusException {@code 404 Not Found} se o ID não existir.
     */
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with id: " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Constrói um {@link Specification} com predicados {@code AND} para os filtros fornecidos.
     *
     * <p>O padrão Specification permite compor queries dinâmicas sem proliferar métodos
     * no repository ou usar queries JPQL com parâmetros condicionais. Cada filtro não-nulo
     * adiciona um predicado; filtros {@code null} são simplesmente ignorados.</p>
     *
     * @return {@code Specification<TodoList>} pronta para ser passada ao {@code JpaSpecificationExecutor}.
     */
    private Specification<TodoList> buildFilter(Boolean completed, LocalDate createdAtStart, LocalDate createdAtEnd,
                                                 LocalDate deadline, Priority priority, Category category, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (completed != null) {
                predicates.add(cb.equal(root.get("completed"), completed));
            }
            if (createdAtStart != null) {
                // Converte LocalDate para LocalDateTime no início do dia (00:00:00)
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAtStart.atStartOfDay()));
            }
            if (createdAtEnd != null) {
                // Converte LocalDate para LocalDateTime no final do dia (23:59:59) para inclusividade
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdAtEnd.atTime(23, 59, 59)));
            }
            if (deadline != null) {
                predicates.add(cb.equal(root.get("deadline"), deadline));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (search != null && !search.isBlank()) {
                // Busca parcial case-insensitive: converte ambos os lados para minúsculas antes do LIKE
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
