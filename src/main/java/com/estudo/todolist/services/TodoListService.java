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

@Service
public class TodoListService {

    private final TodoListRepository repository;

    public TodoListService(TodoListRepository repository) {
        this.repository = repository;
    }

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

    @Transactional(readOnly = true)
    public List<TodoListResponseDTO> findAll(Boolean completed, LocalDate createdAtStart, LocalDate createdAtEnd,
                                             LocalDate deadline, Priority priority, Category category, String search) {
        return repository.findAll(buildFilter(completed, createdAtStart, createdAtEnd, deadline, priority, category, search))
                .stream()
                .map(TodoListResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TodoListResponseDTO findById(Long id) {
        return repository.findById(id)
                .map(TodoListResponseDTO::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with id: " + id));
    }

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

    @Transactional
    public TodoListResponseDTO toggleCompleted(Long id) {
        TodoList entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with id: " + id));
        entity.setCompleted(!entity.isCompleted());
        return TodoListResponseDTO.from(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private Specification<TodoList> buildFilter(Boolean completed, LocalDate createdAtStart, LocalDate createdAtEnd,
                                                 LocalDate deadline, Priority priority, Category category, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (completed != null) {
                predicates.add(cb.equal(root.get("completed"), completed));
            }
            if (createdAtStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAtStart.atStartOfDay()));
            }
            if (createdAtEnd != null) {
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
