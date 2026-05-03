package com.estudo.todolist.controllers;

import com.estudo.todolist.dtos.TodoListAuditDTO;
import com.estudo.todolist.dtos.TodoListRequestDTO;
import com.estudo.todolist.dtos.TodoListResponseDTO;
import com.estudo.todolist.enums.Category;
import com.estudo.todolist.enums.Priority;
import com.estudo.todolist.services.TodoListAuditService;
import com.estudo.todolist.services.TodoListService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller REST que expõe os endpoints de gerenciamento e auditoria de tarefas.
 *
 * <p>Todos os endpoints estão sob o prefixo {@code /api/tasks} e seguem os códigos
 * HTTP semânticos: {@code 201} para criação, {@code 200} para consultas e atualizações,
 * {@code 204} para deleção e {@code 404} quando um ID referenciado não é encontrado.</p>
 *
 * <p>A lógica de negócio é delegada integralmente ao {@link TodoListService} e ao
 * {@link TodoListAuditService}, mantendo este controller restrito ao contrato HTTP.</p>
 */
@RestController
@RequestMapping("/api/tasks")
public class TodoListController {

    private final TodoListService service;
    private final TodoListAuditService auditService;

    public TodoListController(TodoListService service, TodoListAuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    /**
     * Cria uma nova tarefa.
     *
     * @param dto dados da tarefa a ser criada.
     * @return {@code 201 Created} com o objeto persistido no corpo.
     */
    @PostMapping("/create")
    public ResponseEntity<TodoListResponseDTO> create(@RequestBody TodoListRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    /**
     * Lista todas as tarefas com filtros opcionais combinados por {@code AND}.
     *
     * <p>Todos os parâmetros são opcionais. Quando omitidos, retorna todas as tarefas.
     * Datas devem ser enviadas no formato ISO {@code yyyy-MM-dd}.</p>
     *
     * @param completed      filtra por status de conclusão ({@code true} ou {@code false}).
     * @param createdAtStart início do intervalo de data de criação (inclusivo).
     * @param createdAtEnd   fim do intervalo de data de criação (inclusivo).
     * @param deadline       filtra por data-limite exata.
     * @param priority       filtra por prioridade ({@code BAIXO}, {@code MEDIO}, {@code ALTO}).
     * @param category       filtra por categoria ({@code TRABALHO}, {@code ESTUDO}, {@code PESSOAL}).
     * @param search         busca parcial no título ou descrição (case-insensitive).
     * @return {@code 200 OK} com a lista de tarefas filtradas.
     */
    @GetMapping("/read-all")
    public ResponseEntity<List<TodoListResponseDTO>> findAll(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdAtStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdAtEnd,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(service.findAll(completed, createdAtStart, createdAtEnd, deadline, priority, category, search));
    }

    /**
     * Busca uma tarefa pelo seu identificador.
     *
     * @param id identificador da tarefa.
     * @return {@code 200 OK} com os dados da tarefa, ou {@code 404 Not Found} se inexistente.
     */
    @GetMapping("/by-id/{id}")
    public ResponseEntity<TodoListResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Atualiza todos os campos editáveis de uma tarefa existente (operação de substituição total).
     *
     * @param id  identificador da tarefa a ser atualizada.
     * @param dto novos dados a serem aplicados.
     * @return {@code 200 OK} com o estado atualizado, ou {@code 404 Not Found} se inexistente.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<TodoListResponseDTO> update(@PathVariable Long id, @RequestBody TodoListRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    /**
     * Alterna o status de conclusão de uma tarefa ({@code completed}: {@code true} ↔ {@code false}).
     *
     * @param id identificador da tarefa.
     * @return {@code 200 OK} com o novo estado de {@code completed}, ou {@code 404 Not Found} se inexistente.
     */
    @PatchMapping("/done/{id}")
    public ResponseEntity<TodoListResponseDTO> toggleCompleted(@PathVariable Long id) {
        return ResponseEntity.ok(service.toggleCompleted(id));
    }

    /**
     * Remove permanentemente uma tarefa.
     *
     * @param id identificador da tarefa a ser removida.
     * @return {@code 204 No Content} em caso de sucesso, ou {@code 404 Not Found} se inexistente.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retorna o histórico completo de revisões de uma tarefa específica, ordenado da mais recente.
     *
     * <p>Cada entrada representa um snapshot do estado da tarefa no momento em que
     * uma operação (criação, atualização ou deleção) foi registrada pelo Hibernate Envers.</p>
     *
     * @param id identificador da tarefa.
     * @return {@code 200 OK} com a lista de revisões, ou lista vazia se não houver histórico.
     */
    @GetMapping("/{id}/audit")
    public ResponseEntity<List<TodoListAuditDTO>> getAuditByTaskId(@PathVariable Long id) {
        return ResponseEntity.ok(auditService.findAuditByTaskId(id));
    }

    /**
     * Retorna o histórico de revisões de todas as tarefas, ordenado da revisão mais recente.
     *
     * @return {@code 200 OK} com todas as revisões registradas pelo Envers.
     */
    @GetMapping("/all/audit")
    public ResponseEntity<List<TodoListAuditDTO>> getAllAudits() {
        return ResponseEntity.ok(auditService.findAllAudits());
    }
}
