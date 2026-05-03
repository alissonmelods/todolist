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

@RestController
@RequestMapping("/api/tasks")
public class TodoListController {

    private final TodoListService service;
    private final TodoListAuditService auditService;

    public TodoListController(TodoListService service, TodoListAuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @PostMapping("/create")
    public ResponseEntity<TodoListResponseDTO> create(@RequestBody TodoListRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

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

    @GetMapping("/by-id/{id}")
    public ResponseEntity<TodoListResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<TodoListResponseDTO> update(@PathVariable Long id, @RequestBody TodoListRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PatchMapping("/done/{id}")
    public ResponseEntity<TodoListResponseDTO> toggleCompleted(@PathVariable Long id) {
        return ResponseEntity.ok(service.toggleCompleted(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<List<TodoListAuditDTO>> getAuditByTaskId(@PathVariable Long id) {
        return ResponseEntity.ok(auditService.findAuditByTaskId(id));
    }

    @GetMapping("/all/audit")
    public ResponseEntity<List<TodoListAuditDTO>> getAllAudits() {
        return ResponseEntity.ok(auditService.findAllAudits());
    }
}
