package com.estudo.todolist.controllers;

import com.estudo.todolist.dtos.TodoListAuditDTO;
import com.estudo.todolist.dtos.TodoListRequestDTO;
import com.estudo.todolist.dtos.TodoListResponseDTO;
import com.estudo.todolist.enums.Category;
import com.estudo.todolist.enums.Priority;
import com.estudo.todolist.services.TodoListAuditService;
import com.estudo.todolist.services.TodoListService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração da camada web para {@link TodoListController}.
 *
 * <p>{@code @WebMvcTest} inicializa apenas a fatia web do Spring (DispatcherServlet,
 * filtros, converters Jackson), sem banco de dados ou contexto completo. Os serviços
 * são substituídos por {@code @MockBean}, garantindo isolamento total da camada HTTP.</p>
 *
 * <p>As requisições são executadas via {@link MockMvc} — sem servidor embarcado — e
 * o context path configurado no {@code application.properties} não é aplicado aqui
 * (os endpoints são acessados diretamente pelo path do {@code @RequestMapping}).</p>
 */
@WebMvcTest(TodoListController.class)
@DisplayName("TodoListController - Testes de Integração Web")
class TodoListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /** ObjectMapper configurado pelo Spring Boot com suporte a Java Time (LocalDate, LocalDateTime). */
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoListService service;

    @MockBean
    private TodoListAuditService auditService;

    // ── Constantes ────────────────────────────────────────────────────────────

    private static final Long   ID_EXISTENTE   = 1L;
    private static final Long   ID_INEXISTENTE = 99L;
    private static final String BASE_URL       = "/api/tasks";

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private TodoListResponseDTO responseFixture;
    private TodoListRequestDTO  requestFixture;

    @BeforeEach
    void configurar() {
        responseFixture = new TodoListResponseDTO(
                ID_EXISTENTE,
                LocalDateTime.of(2026, 5, 1, 10, 0),
                LocalDateTime.of(2026, 5, 1, 10, 0),
                "Estudar Spring Boot",
                "Revisar módulo de testes",
                false,
                LocalDate.of(2026, 6, 1),
                Priority.ALTO,
                Category.ESTUDO
        );

        requestFixture = new TodoListRequestDTO(
                "Estudar Spring Boot",
                "Revisar módulo de testes",
                LocalDate.of(2026, 6, 1),
                Priority.ALTO,
                Category.ESTUDO
        );
    }

    // =========================================================================
    // POST /api/tasks/create  →  201 Created
    // =========================================================================

    @Nested
    @DisplayName("POST /create")
    class Create {

        @Test
        @DisplayName("deveRetornarStatus201AoCriarTarefaComBodyValido")
        void deveRetornarStatus201AoCriarTarefaComBodyValido() throws Exception {
            // Given
            when(service.create(any(TodoListRequestDTO.class))).thenReturn(responseFixture);

            // When / Then
            mockMvc.perform(post(BASE_URL + "/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestFixture)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(ID_EXISTENTE))
                    .andExpect(jsonPath("$.title").value("Estudar Spring Boot"))
                    .andExpect(jsonPath("$.completed").value(false))
                    .andExpect(jsonPath("$.priority").value("ALTO"))
                    .andExpect(jsonPath("$.category").value("ESTUDO"));
        }

        @Test
        @DisplayName("deveRetornarStatus201ComTodosOsCamposDaRespostaPreenchidos")
        void deveRetornarStatus201ComTodosOsCamposDaRespostaPreenchidos() throws Exception {
            // Given
            when(service.create(any(TodoListRequestDTO.class))).thenReturn(responseFixture);

            // When / Then
            mockMvc.perform(post(BASE_URL + "/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestFixture)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists())
                    .andExpect(jsonPath("$.title").value("Estudar Spring Boot"))
                    .andExpect(jsonPath("$.description").value("Revisar módulo de testes"))
                    .andExpect(jsonPath("$.completed").value(false))
                    .andExpect(jsonPath("$.deadline").value("2026-06-01"))
                    .andExpect(jsonPath("$.priority").value("ALTO"))
                    .andExpect(jsonPath("$.category").value("ESTUDO"));
        }

        @Test
        @DisplayName("deveChamarServiceCreateExatamenteUmaVez")
        void deveChamarServiceCreateExatamenteUmaVez() throws Exception {
            // Given
            when(service.create(any(TodoListRequestDTO.class))).thenReturn(responseFixture);

            // When
            mockMvc.perform(post(BASE_URL + "/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestFixture)));

            // Then
            verify(service, times(1)).create(any(TodoListRequestDTO.class));
        }
    }

    // =========================================================================
    // GET /api/tasks/read-all  →  200 OK
    // =========================================================================

    @Nested
    @DisplayName("GET /read-all")
    class ReadAll {

        @Test
        @DisplayName("deveRetornarStatus200ComListaPreenchidaQuandoExistemTarefas")
        void deveRetornarStatus200ComListaPreenchidaQuandoExistemTarefas() throws Exception {
            // Given
            when(service.findAll(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(responseFixture));

            // When / Then
            mockMvc.perform(get(BASE_URL + "/read-all"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(ID_EXISTENTE))
                    .andExpect(jsonPath("$[0].title").value("Estudar Spring Boot"));
        }

        @Test
        @DisplayName("deveRetornarStatus200ComListaVaziaQuandoNaoHaTarefas")
        void deveRetornarStatus200ComListaVaziaQuandoNaoHaTarefas() throws Exception {
            // Given
            when(service.findAll(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            // When / Then
            mockMvc.perform(get(BASE_URL + "/read-all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("deveRetornarStatus200AplicandoFiltroDeCompleted")
        void deveRetornarStatus200AplicandoFiltroDeCompleted() throws Exception {
            // Given
            when(service.findAll(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(responseFixture));

            // When / Then
            mockMvc.perform(get(BASE_URL + "/read-all")
                            .param("completed", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].completed").value(false));
        }

        @Test
        @DisplayName("deveRetornarStatus200AplicandoFiltroDePrioridade")
        void deveRetornarStatus200AplicandoFiltroDePrioridade() throws Exception {
            // Given
            when(service.findAll(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(responseFixture));

            // When / Then
            mockMvc.perform(get(BASE_URL + "/read-all")
                            .param("priority", "ALTO"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].priority").value("ALTO"));
        }

        @Test
        @DisplayName("deveRetornarStatus200AplicandoFiltroDeCategoria")
        void deveRetornarStatus200AplicandoFiltroDeCategoria() throws Exception {
            // Given
            when(service.findAll(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(responseFixture));

            // When / Then
            mockMvc.perform(get(BASE_URL + "/read-all")
                            .param("category", "ESTUDO"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].category").value("ESTUDO"));
        }

        @Test
        @DisplayName("deveRetornarStatus200AplicandoFiltroDeBuscaDeTexto")
        void deveRetornarStatus200AplicandoFiltroDeBuscaDeTexto() throws Exception {
            // Given
            when(service.findAll(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(responseFixture));

            // When / Then
            mockMvc.perform(get(BASE_URL + "/read-all")
                            .param("search", "Spring"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Estudar Spring Boot"));
        }

        @Test
        @DisplayName("deveRetornarStatus200AplicandoIntervaloDeDataDeCriacao")
        void deveRetornarStatus200AplicandoIntervaloDeDataDeCriacao() throws Exception {
            // Given
            when(service.findAll(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(responseFixture));

            // When / Then
            mockMvc.perform(get(BASE_URL + "/read-all")
                            .param("createdAtStart", "2026-05-01")
                            .param("createdAtEnd", "2026-05-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @DisplayName("deveRetornarStatus200ComTodosOsFiltrosSimultaneamente")
        void deveRetornarStatus200ComTodosOsFiltrosSimultaneamente() throws Exception {
            // Given
            when(service.findAll(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(responseFixture));

            // When / Then
            mockMvc.perform(get(BASE_URL + "/read-all")
                            .param("completed", "false")
                            .param("createdAtStart", "2026-05-01")
                            .param("createdAtEnd", "2026-05-31")
                            .param("deadline", "2026-06-01")
                            .param("priority", "ALTO")
                            .param("category", "ESTUDO")
                            .param("search", "Spring"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }

    // =========================================================================
    // GET /api/tasks/by-id/{id}  →  200 OK  /  404 Not Found
    // =========================================================================

    @Nested
    @DisplayName("GET /by-id/{id}")
    class FindById {

        @Test
        @DisplayName("deveRetornarStatus200AoBuscarTarefaPorIdExistente")
        void deveRetornarStatus200AoBuscarTarefaPorIdExistente() throws Exception {
            // Given
            when(service.findById(ID_EXISTENTE)).thenReturn(responseFixture);

            // When / Then
            mockMvc.perform(get(BASE_URL + "/by-id/{id}", ID_EXISTENTE))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(ID_EXISTENTE))
                    .andExpect(jsonPath("$.title").value("Estudar Spring Boot"))
                    .andExpect(jsonPath("$.priority").value("ALTO"))
                    .andExpect(jsonPath("$.category").value("ESTUDO"))
                    .andExpect(jsonPath("$.completed").value(false));
        }

        @Test
        @DisplayName("deveRetornarStatus404AoBuscarTarefaPorIdInexistente")
        void deveRetornarStatus404AoBuscarTarefaPorIdInexistente() throws Exception {
            // Given
            when(service.findById(ID_INEXISTENTE))
                    .thenThrow(new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Task not found with id: " + ID_INEXISTENTE));

            // When / Then
            mockMvc.perform(get(BASE_URL + "/by-id/{id}", ID_INEXISTENTE))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // PUT /api/tasks/update/{id}  →  200 OK  /  404 Not Found
    // =========================================================================

    @Nested
    @DisplayName("PUT /update/{id}")
    class Update {

        @Test
        @DisplayName("deveRetornarStatus200AoAtualizarTarefaExistente")
        void deveRetornarStatus200AoAtualizarTarefaExistente() throws Exception {
            // Given
            TodoListRequestDTO requestAtualizado = new TodoListRequestDTO(
                    "Título Atualizado", "Nova descrição",
                    LocalDate.of(2026, 7, 1), Priority.MEDIO, Category.TRABALHO);
            TodoListResponseDTO responseAtualizado = new TodoListResponseDTO(
                    ID_EXISTENTE,
                    LocalDateTime.of(2026, 5, 1, 10, 0),
                    LocalDateTime.of(2026, 5, 3, 12, 0),
                    "Título Atualizado", "Nova descrição",
                    false, LocalDate.of(2026, 7, 1), Priority.MEDIO, Category.TRABALHO);

            when(service.update(eq(ID_EXISTENTE), any(TodoListRequestDTO.class)))
                    .thenReturn(responseAtualizado);

            // When / Then
            mockMvc.perform(put(BASE_URL + "/update/{id}", ID_EXISTENTE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestAtualizado)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ID_EXISTENTE))
                    .andExpect(jsonPath("$.title").value("Título Atualizado"))
                    .andExpect(jsonPath("$.priority").value("MEDIO"))
                    .andExpect(jsonPath("$.category").value("TRABALHO"));
        }

        @Test
        @DisplayName("deveRetornarStatus404AoAtualizarTarefaInexistente")
        void deveRetornarStatus404AoAtualizarTarefaInexistente() throws Exception {
            // Given
            when(service.update(eq(ID_INEXISTENTE), any(TodoListRequestDTO.class)))
                    .thenThrow(new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Task not found with id: " + ID_INEXISTENTE));

            // When / Then
            mockMvc.perform(put(BASE_URL + "/update/{id}", ID_INEXISTENTE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestFixture)))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // PATCH /api/tasks/done/{id}  →  200 OK  /  404 Not Found
    // =========================================================================

    @Nested
    @DisplayName("PATCH /done/{id}")
    class ToggleCompleted {

        @Test
        @DisplayName("deveRetornarStatus200ComCompletedTrueAoMarcarTarefaComoConcluida")
        void deveRetornarStatus200ComCompletedTrueAoMarcarTarefaComoConcluida() throws Exception {
            // Given
            TodoListResponseDTO responseConcluido = new TodoListResponseDTO(
                    ID_EXISTENTE,
                    LocalDateTime.of(2026, 5, 1, 10, 0),
                    LocalDateTime.of(2026, 5, 3, 12, 0),
                    "Estudar Spring Boot", "Revisar módulo de testes",
                    true,   // completed invertido para true
                    LocalDate.of(2026, 6, 1), Priority.ALTO, Category.ESTUDO);

            when(service.toggleCompleted(ID_EXISTENTE)).thenReturn(responseConcluido);

            // When / Then
            mockMvc.perform(patch(BASE_URL + "/done/{id}", ID_EXISTENTE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ID_EXISTENTE))
                    .andExpect(jsonPath("$.completed").value(true));
        }

        @Test
        @DisplayName("deveRetornarStatus200ComCompletedFalseAoReabrirTarefa")
        void deveRetornarStatus200ComCompletedFalseAoReabrirTarefa() throws Exception {
            // Given
            TodoListResponseDTO responseReaberta = new TodoListResponseDTO(
                    ID_EXISTENTE,
                    LocalDateTime.of(2026, 5, 1, 10, 0),
                    LocalDateTime.of(2026, 5, 4, 9, 0),
                    "Estudar Spring Boot", "Revisar módulo de testes",
                    false,  // completed invertido para false
                    LocalDate.of(2026, 6, 1), Priority.ALTO, Category.ESTUDO);

            when(service.toggleCompleted(ID_EXISTENTE)).thenReturn(responseReaberta);

            // When / Then
            mockMvc.perform(patch(BASE_URL + "/done/{id}", ID_EXISTENTE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completed").value(false));
        }

        @Test
        @DisplayName("deveRetornarStatus404AoAlternarStatusDeTarefaInexistente")
        void deveRetornarStatus404AoAlternarStatusDeTarefaInexistente() throws Exception {
            // Given
            when(service.toggleCompleted(ID_INEXISTENTE))
                    .thenThrow(new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Task not found with id: " + ID_INEXISTENTE));

            // When / Then
            mockMvc.perform(patch(BASE_URL + "/done/{id}", ID_INEXISTENTE))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // DELETE /api/tasks/delete/{id}  →  204 No Content  /  404 Not Found
    // =========================================================================

    @Nested
    @DisplayName("DELETE /delete/{id}")
    class Delete {

        @Test
        @DisplayName("deveRetornarStatus204SemCorpoAoDeletarTarefaExistente")
        void deveRetornarStatus204SemCorpoAoDeletarTarefaExistente() throws Exception {
            // Given
            doNothing().when(service).delete(ID_EXISTENTE);

            // When / Then
            mockMvc.perform(delete(BASE_URL + "/delete/{id}", ID_EXISTENTE))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));   // 204 não deve ter corpo

            verify(service, times(1)).delete(ID_EXISTENTE);
        }

        @Test
        @DisplayName("deveRetornarStatus404AoDeletarTarefaInexistente")
        void deveRetornarStatus404AoDeletarTarefaInexistente() throws Exception {
            // Given
            doThrow(new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Task not found with id: " + ID_INEXISTENTE))
                    .when(service).delete(ID_INEXISTENTE);

            // When / Then
            mockMvc.perform(delete(BASE_URL + "/delete/{id}", ID_INEXISTENTE))
                    .andExpect(status().isNotFound());

            verify(service, times(1)).delete(ID_INEXISTENTE);
        }
    }

    // =========================================================================
    // GET /api/tasks/{id}/audit  →  200 OK
    // =========================================================================

    @Nested
    @DisplayName("GET /{id}/audit")
    class AuditById {

        @Test
        @DisplayName("deveRetornarStatus200ComHistoricoDeRevisoesParaIdExistente")
        void deveRetornarStatus200ComHistoricoDeRevisoesParaIdExistente() throws Exception {
            // Given
            TodoListAuditDTO auditCreate = new TodoListAuditDTO(
                    ID_EXISTENTE,
                    LocalDateTime.of(2026, 5, 1, 10, 0), LocalDateTime.of(2026, 5, 1, 10, 0),
                    "Estudar Spring Boot", "Revisar módulo de testes",
                    false, LocalDate.of(2026, 6, 1), Priority.ALTO, Category.ESTUDO,
                    1L, LocalDateTime.of(2026, 5, 1, 10, 0), "CREATE");

            when(auditService.findAuditByTaskId(ID_EXISTENTE)).thenReturn(List.of(auditCreate));

            // When / Then
            mockMvc.perform(get(BASE_URL + "/{id}/audit", ID_EXISTENTE))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].revisionId").value(1))
                    .andExpect(jsonPath("$[0].revisionType").value("CREATE"))
                    .andExpect(jsonPath("$[0].title").value("Estudar Spring Boot"))
                    .andExpect(jsonPath("$[0].priority").value("ALTO"));
        }

        @Test
        @DisplayName("deveRetornarStatus200ComMultiplasRevisoesOrdenadaDoMaisRecente")
        void deveRetornarStatus200ComMultiplasRevisoesOrdenadaDoMaisRecente() throws Exception {
            // Given — auditoria retorna UPDATE antes de CREATE (ordem: mais recente primeiro)
            TodoListAuditDTO auditUpdate = new TodoListAuditDTO(
                    ID_EXISTENTE,
                    LocalDateTime.of(2026, 5, 1, 10, 0), LocalDateTime.of(2026, 5, 3, 14, 0),
                    "Estudar Spring Boot (v2)", null,
                    true, LocalDate.of(2026, 6, 1), Priority.MEDIO, Category.ESTUDO,
                    2L, LocalDateTime.of(2026, 5, 3, 14, 0), "UPDATE");

            TodoListAuditDTO auditCreate = new TodoListAuditDTO(
                    ID_EXISTENTE,
                    LocalDateTime.of(2026, 5, 1, 10, 0), LocalDateTime.of(2026, 5, 1, 10, 0),
                    "Estudar Spring Boot", null,
                    false, LocalDate.of(2026, 6, 1), Priority.ALTO, Category.ESTUDO,
                    1L, LocalDateTime.of(2026, 5, 1, 10, 0), "CREATE");

            when(auditService.findAuditByTaskId(ID_EXISTENTE))
                    .thenReturn(List.of(auditUpdate, auditCreate));

            // When / Then
            mockMvc.perform(get(BASE_URL + "/{id}/audit", ID_EXISTENTE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].revisionType").value("UPDATE"))
                    .andExpect(jsonPath("$[0].revisionId").value(2))
                    .andExpect(jsonPath("$[1].revisionType").value("CREATE"))
                    .andExpect(jsonPath("$[1].revisionId").value(1));
        }

        @Test
        @DisplayName("deveRetornarStatus200ComListaVaziaParaIdSemHistorico")
        void deveRetornarStatus200ComListaVaziaParaIdSemHistorico() throws Exception {
            // Given — ID existe mas ainda não tem revisões registradas
            when(auditService.findAuditByTaskId(ID_INEXISTENTE)).thenReturn(Collections.emptyList());

            // When / Then
            mockMvc.perform(get(BASE_URL + "/{id}/audit", ID_INEXISTENTE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // =========================================================================
    // GET /api/tasks/all/audit  →  200 OK
    // =========================================================================

    @Nested
    @DisplayName("GET /all/audit")
    class AllAudits {

        @Test
        @DisplayName("deveRetornarStatus200ComTodasAsRevisoesOrdenadas")
        void deveRetornarStatus200ComTodasAsRevisoesOrdenadas() throws Exception {
            // Given
            TodoListAuditDTO auditDelete = new TodoListAuditDTO(
                    ID_EXISTENTE,
                    LocalDateTime.of(2026, 5, 1, 10, 0), LocalDateTime.of(2026, 5, 4, 9, 0),
                    "Estudar Spring Boot", null,
                    true, null, Priority.ALTO, Category.ESTUDO,
                    3L, LocalDateTime.of(2026, 5, 4, 9, 0), "DELETE");

            TodoListAuditDTO auditCreate = new TodoListAuditDTO(
                    ID_EXISTENTE,
                    LocalDateTime.of(2026, 5, 1, 10, 0), LocalDateTime.of(2026, 5, 1, 10, 0),
                    "Estudar Spring Boot", null,
                    false, null, Priority.ALTO, Category.ESTUDO,
                    1L, LocalDateTime.of(2026, 5, 1, 10, 0), "CREATE");

            when(auditService.findAllAudits()).thenReturn(List.of(auditDelete, auditCreate));

            // When / Then
            mockMvc.perform(get(BASE_URL + "/all/audit"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].revisionType").value("DELETE"))
                    .andExpect(jsonPath("$[0].revisionId").value(3))
                    .andExpect(jsonPath("$[1].revisionType").value("CREATE"))
                    .andExpect(jsonPath("$[1].revisionId").value(1));
        }

        @Test
        @DisplayName("deveRetornarStatus200ComListaVaziaQuandoNaoHaAuditorias")
        void deveRetornarStatus200ComListaVaziaQuandoNaoHaAuditorias() throws Exception {
            // Given
            when(auditService.findAllAudits()).thenReturn(Collections.emptyList());

            // When / Then
            mockMvc.perform(get(BASE_URL + "/all/audit"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }
}
