package com.estudo.todolist.services;

import com.estudo.todolist.dtos.TodoListRequestDTO;
import com.estudo.todolist.dtos.TodoListResponseDTO;
import com.estudo.todolist.entities.TodoList;
import com.estudo.todolist.enums.Category;
import com.estudo.todolist.enums.Priority;
import com.estudo.todolist.repositories.TodoListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes de unidade para {@link TodoListService}.
 *
 * <p>O repositório é mockado via Mockito, isolando completamente a camada de serviço
 * sem necessidade de banco de dados ou contexto Spring.</p>
 */
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
@DisplayName("TodoListService")
class TodoListServiceTest {

    @Mock
    private TodoListRepository repository;

    @InjectMocks
    private TodoListService service;

    // ── Constantes de apoio ───────────────────────────────────────────────────

    private static final Long ID_EXISTENTE   = 1L;
    private static final Long ID_INEXISTENTE = 99L;

    // ── Fixtures reutilizadas nos testes ──────────────────────────────────────

    private TodoList         tarefaFixture;
    private TodoListRequestDTO requestFixture;

    @BeforeEach
    void configurar() {
        tarefaFixture = TodoList.builder()
                .id(ID_EXISTENTE)
                .title("Estudar Spring Boot")
                .description("Revisar módulo de testes")
                .completed(false)
                .deadline(LocalDate.of(2026, 6, 1))
                .priority(Priority.ALTO)
                .category(Category.ESTUDO)
                .createdAt(LocalDateTime.of(2026, 5, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 5, 1, 10, 0))
                .build();

        requestFixture = new TodoListRequestDTO(
                "Estudar Spring Boot",
                "Revisar módulo de testes",
                LocalDate.of(2026, 6, 1),
                Priority.ALTO,
                Category.ESTUDO
        );
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deveSalvarTarefaERetornarDTOComIdGerado")
        void deveSalvarTarefaERetornarDTOComIdGerado() {
            when(repository.save(any(TodoList.class))).thenReturn(tarefaFixture);

            TodoListResponseDTO resultado = service.create(requestFixture);

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
            assertThat(resultado.title()).isEqualTo("Estudar Spring Boot");
            verify(repository, times(1)).save(any(TodoList.class));
        }

        @Test
        @DisplayName("deveSempreIniciarCompletedComoFalso")
        void deveSempreIniciarCompletedComoFalso() {
            when(repository.save(any(TodoList.class))).thenReturn(tarefaFixture);

            service.create(requestFixture);

            ArgumentCaptor<TodoList> captor = ArgumentCaptor.forClass(TodoList.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().isCompleted()).isFalse();
        }

        @Test
        @DisplayName("deveMappearTodosOsCamposDoRequestParaEntidade")
        void deveMappearTodosOsCamposDoRequestParaEntidade() {
            when(repository.save(any(TodoList.class))).thenReturn(tarefaFixture);

            service.create(requestFixture);

            ArgumentCaptor<TodoList> captor = ArgumentCaptor.forClass(TodoList.class);
            verify(repository).save(captor.capture());
            TodoList entidadeSalva = captor.getValue();

            assertThat(entidadeSalva.getTitle()).isEqualTo(requestFixture.title());
            assertThat(entidadeSalva.getDescription()).isEqualTo(requestFixture.description());
            assertThat(entidadeSalva.getDeadline()).isEqualTo(requestFixture.deadline());
            assertThat(entidadeSalva.getPriority()).isEqualTo(requestFixture.priority());
            assertThat(entidadeSalva.getCategory()).isEqualTo(requestFixture.category());
        }

        @Test
        @DisplayName("deveCriarTarefaSemDescricaoEDeadlineOpcionais")
        void deveCriarTarefaSemDescricaoEDeadlineOpcionais() {
            TodoListRequestDTO requestMinimo = new TodoListRequestDTO(
                    "Tarefa Simples", null, null, Priority.MEDIO, Category.PESSOAL);
            TodoList entidadeMinima = TodoList.builder()
                    .id(2L).title("Tarefa Simples").priority(Priority.MEDIO)
                    .category(Category.PESSOAL).completed(false).build();

            when(repository.save(any(TodoList.class))).thenReturn(entidadeMinima);

            TodoListResponseDTO resultado = service.create(requestMinimo);

            assertThat(resultado.description()).isNull();
            assertThat(resultado.deadline()).isNull();
        }
    }

    // =========================================================================
    // FIND ALL
    // =========================================================================

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("deveRetornarListaPreenchidaQuandoExistemTarefas")
        void deveRetornarListaPreenchidaQuandoExistemTarefas() {
            when(repository.findAll(any(Specification.class))).thenReturn(List.of(tarefaFixture));

            List<TodoListResponseDTO> resultado = service.findAll(null, null, null, null, null, null, null);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).title()).isEqualTo("Estudar Spring Boot");
        }

        @Test
        @DisplayName("deveRetornarListaVaziaQuandoNaoHaTarefas")
        void deveRetornarListaVaziaQuandoNaoHaTarefas() {
            when(repository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

            List<TodoListResponseDTO> resultado = service.findAll(null, null, null, null, null, null, null);

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("deveMappearCorretamenteTodosOsCamposNaListagem")
        void deveMappearCorretamenteTodosOsCamposNaListagem() {
            when(repository.findAll(any(Specification.class))).thenReturn(List.of(tarefaFixture));

            TodoListResponseDTO dto = service.findAll(null, null, null, null, null, null, null).get(0);

            assertThat(dto.id()).isEqualTo(tarefaFixture.getId());
            assertThat(dto.title()).isEqualTo(tarefaFixture.getTitle());
            assertThat(dto.description()).isEqualTo(tarefaFixture.getDescription());
            assertThat(dto.completed()).isFalse();
            assertThat(dto.priority()).isEqualTo(Priority.ALTO);
            assertThat(dto.category()).isEqualTo(Category.ESTUDO);
            assertThat(dto.deadline()).isEqualTo(tarefaFixture.getDeadline());
        }

        @Test
        @DisplayName("deveDelegarAoRepositorioMesmoComTodosOsFiltrosNulos")
        void deveDelegarAoRepositorioMesmoComTodosOsFiltrosNulos() {
            when(repository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

            service.findAll(null, null, null, null, null, null, null);

            verify(repository, times(1)).findAll(any(Specification.class));
        }

        @Test
        @DisplayName("deveDelegarAoRepositorioComFiltroDeCompleted")
        void deveDelegarAoRepositorioComFiltroDeCompleted() {
            when(repository.findAll(any(Specification.class))).thenReturn(List.of(tarefaFixture));

            service.findAll(false, null, null, null, null, null, null);

            verify(repository, times(1)).findAll(any(Specification.class));
        }

        @Test
        @DisplayName("deveDelegarAoRepositorioComIntervaloDeDataDeCriacao")
        void deveDelegarAoRepositorioComIntervaloDeDataDeCriacao() {
            when(repository.findAll(any(Specification.class))).thenReturn(List.of(tarefaFixture));

            service.findAll(null,
                    LocalDate.of(2026, 5, 1),
                    LocalDate.of(2026, 5, 31),
                    null, null, null, null);

            verify(repository, times(1)).findAll(any(Specification.class));
        }

        @Test
        @DisplayName("deveDelegarAoRepositorioComFiltroDeTextoBusca")
        void deveDelegarAoRepositorioComFiltroDeTextoBusca() {
            when(repository.findAll(any(Specification.class))).thenReturn(List.of(tarefaFixture));

            service.findAll(null, null, null, null, null, null, "Spring");

            verify(repository, times(1)).findAll(any(Specification.class));
        }

        @Test
        @DisplayName("deveDelegarAoRepositorioComTodosOsFiltrosPreenchidos")
        void deveDelegarAoRepositorioComTodosOsFiltrosPreenchidos() {
            when(repository.findAll(any(Specification.class))).thenReturn(List.of(tarefaFixture));
            LocalDate data = LocalDate.of(2026, 5, 1);

            service.findAll(false, data, data, data, Priority.ALTO, Category.ESTUDO, "Spring");

            verify(repository, times(1)).findAll(any(Specification.class));
        }
    }

    // =========================================================================
    // FIND BY ID
    // =========================================================================

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("deveRetornarTarefaQuandoIdExiste")
        void deveRetornarTarefaQuandoIdExiste() {
            when(repository.findById(ID_EXISTENTE)).thenReturn(Optional.of(tarefaFixture));

            TodoListResponseDTO resultado = service.findById(ID_EXISTENTE);

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
            assertThat(resultado.title()).isEqualTo("Estudar Spring Boot");
        }

        @Test
        @DisplayName("deveLancarExcecaoQuandoIdNaoExistir")
        void deveLancarExcecaoQuandoIdNaoExistir() {
            when(repository.findById(ID_INEXISTENTE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(ID_INEXISTENTE))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
                        assertThat(rse.getReason()).contains(String.valueOf(ID_INEXISTENTE));
                    });
        }

        @Test
        @DisplayName("deveLancarExcecaoComMensagemContendoOIdInexistente")
        void deveLancarExcecaoComMensagemContendoOIdInexistente() {
            when(repository.findById(ID_INEXISTENTE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(ID_INEXISTENTE))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining(String.valueOf(ID_INEXISTENTE));
        }
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("deveAtualizarTarefaComSucesso")
        void deveAtualizarTarefaComSucesso() {
            TodoListRequestDTO requestAtualizado = new TodoListRequestDTO(
                    "Título Atualizado", "Descrição Atualizada",
                    LocalDate.of(2026, 7, 1), Priority.MEDIO, Category.TRABALHO);
            TodoList entidadeAtualizada = TodoList.builder()
                    .id(ID_EXISTENTE).title("Título Atualizado").description("Descrição Atualizada")
                    .deadline(LocalDate.of(2026, 7, 1)).priority(Priority.MEDIO)
                    .category(Category.TRABALHO).completed(false).build();

            when(repository.findById(ID_EXISTENTE)).thenReturn(Optional.of(tarefaFixture));
            when(repository.save(any(TodoList.class))).thenReturn(entidadeAtualizada);

            TodoListResponseDTO resultado = service.update(ID_EXISTENTE, requestAtualizado);

            assertThat(resultado.title()).isEqualTo("Título Atualizado");
            assertThat(resultado.description()).isEqualTo("Descrição Atualizada");
            assertThat(resultado.priority()).isEqualTo(Priority.MEDIO);
            assertThat(resultado.category()).isEqualTo(Category.TRABALHO);
        }

        @Test
        @DisplayName("deveAtualizarApenasCamposEditaveisNaEntidade")
        void deveAtualizarApenasCamposEditaveisNaEntidade() {
            TodoListRequestDTO novosDados = new TodoListRequestDTO(
                    "Novo Título", "Nova Descrição",
                    LocalDate.of(2026, 8, 1), Priority.BAIXO, Category.PESSOAL);

            when(repository.findById(ID_EXISTENTE)).thenReturn(Optional.of(tarefaFixture));
            when(repository.save(any(TodoList.class))).thenReturn(tarefaFixture);

            service.update(ID_EXISTENTE, novosDados);

            // Verifica que somente os campos permitidos foram alterados na entidade antes do save
            ArgumentCaptor<TodoList> captor = ArgumentCaptor.forClass(TodoList.class);
            verify(repository).save(captor.capture());
            TodoList entidadeSalva = captor.getValue();

            assertThat(entidadeSalva.getTitle()).isEqualTo("Novo Título");
            assertThat(entidadeSalva.getDescription()).isEqualTo("Nova Descrição");
            assertThat(entidadeSalva.getDeadline()).isEqualTo(LocalDate.of(2026, 8, 1));
            assertThat(entidadeSalva.getPriority()).isEqualTo(Priority.BAIXO);
            assertThat(entidadeSalva.getCategory()).isEqualTo(Category.PESSOAL);
        }

        @Test
        @DisplayName("deveNaoAlterarCompletedDuranteUpdate")
        void deveNaoAlterarCompletedDuranteUpdate() {
            tarefaFixture.setCompleted(true);
            when(repository.findById(ID_EXISTENTE)).thenReturn(Optional.of(tarefaFixture));
            when(repository.save(any(TodoList.class))).thenReturn(tarefaFixture);

            service.update(ID_EXISTENTE, requestFixture);

            ArgumentCaptor<TodoList> captor = ArgumentCaptor.forClass(TodoList.class);
            verify(repository).save(captor.capture());
            // O campo completed não deve ser tocado pelo update()
            assertThat(captor.getValue().isCompleted()).isTrue();
        }

        @Test
        @DisplayName("deveLancarExcecaoAoAtualizarIdInexistente")
        void deveLancarExcecaoAoAtualizarIdInexistente() {
            when(repository.findById(ID_INEXISTENTE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(ID_INEXISTENTE, requestFixture))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value())
                            .isEqualTo(HttpStatus.NOT_FOUND.value()));

            verify(repository, never()).save(any());
        }
    }

    // =========================================================================
    // TOGGLE COMPLETED
    // =========================================================================

    @Nested
    @DisplayName("toggleCompleted()")
    class ToggleCompleted {

        @Test
        @DisplayName("deveMudarCompletedDeFalsoParaVerdadeiro")
        void deveMudarCompletedDeFalsoParaVerdadeiro() {
            tarefaFixture.setCompleted(false);
            TodoList tarefaConcluida = TodoList.builder()
                    .id(ID_EXISTENTE).title(tarefaFixture.getTitle())
                    .priority(Priority.ALTO).category(Category.ESTUDO).completed(true).build();

            when(repository.findById(ID_EXISTENTE)).thenReturn(Optional.of(tarefaFixture));
            when(repository.save(any(TodoList.class))).thenReturn(tarefaConcluida);

            TodoListResponseDTO resultado = service.toggleCompleted(ID_EXISTENTE);

            assertThat(resultado.completed()).isTrue();
        }

        @Test
        @DisplayName("deveMudarCompletedDeVerdadeiroParaFalso")
        void deveMudarCompletedDeVerdadeiroParaFalso() {
            tarefaFixture.setCompleted(true);
            TodoList tarefaReaberta = TodoList.builder()
                    .id(ID_EXISTENTE).title(tarefaFixture.getTitle())
                    .priority(Priority.ALTO).category(Category.ESTUDO).completed(false).build();

            when(repository.findById(ID_EXISTENTE)).thenReturn(Optional.of(tarefaFixture));
            when(repository.save(any(TodoList.class))).thenReturn(tarefaReaberta);

            TodoListResponseDTO resultado = service.toggleCompleted(ID_EXISTENTE);

            assertThat(resultado.completed()).isFalse();
        }

        @Test
        @DisplayName("deveInverterCompletedNaEntidadeAntesDeSalvar")
        void deveInverterCompletedNaEntidadeAntesDeSalvar() {
            tarefaFixture.setCompleted(false);
            when(repository.findById(ID_EXISTENTE)).thenReturn(Optional.of(tarefaFixture));
            when(repository.save(any(TodoList.class))).thenReturn(tarefaFixture);

            service.toggleCompleted(ID_EXISTENTE);

            ArgumentCaptor<TodoList> captor = ArgumentCaptor.forClass(TodoList.class);
            verify(repository).save(captor.capture());
            // Confirma que a inversão ocorreu na entidade antes de chegar ao save
            assertThat(captor.getValue().isCompleted()).isTrue();
        }

        @Test
        @DisplayName("deveLancarExcecaoAoToggleIdInexistente")
        void deveLancarExcecaoAoToggleIdInexistente() {
            when(repository.findById(ID_INEXISTENTE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.toggleCompleted(ID_INEXISTENTE))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value())
                            .isEqualTo(HttpStatus.NOT_FOUND.value()));

            verify(repository, never()).save(any());
        }
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("deveChamarDeleteByIdQuandoTarefaExiste")
        void deveChamarDeleteByIdQuandoTarefaExiste() {
            when(repository.existsById(ID_EXISTENTE)).thenReturn(true);

            service.delete(ID_EXISTENTE);

            verify(repository, times(1)).deleteById(ID_EXISTENTE);
        }

        @Test
        @DisplayName("deveLancarExcecaoAoDeletarIdInexistente")
        void deveLancarExcecaoAoDeletarIdInexistente() {
            when(repository.existsById(ID_INEXISTENTE)).thenReturn(false);

            assertThatThrownBy(() -> service.delete(ID_INEXISTENTE))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value())
                            .isEqualTo(HttpStatus.NOT_FOUND.value()));

            verify(repository, never()).deleteById(any());
        }

        @Test
        @DisplayName("deveNaoChamarDeleteByIdQuandoIdNaoExiste")
        void deveNaoChamarDeleteByIdQuandoIdNaoExiste() {
            when(repository.existsById(ID_INEXISTENTE)).thenReturn(false);

            try {
                service.delete(ID_INEXISTENTE);
            } catch (ResponseStatusException ignored) { }

            verify(repository, never()).deleteById(any());
        }
    }

    // =========================================================================
    // ENUMS — Comportamento com Priority e Category
    // =========================================================================

    @Nested
    @DisplayName("Comportamento com Enums")
    class EnumBehavior {

        // ── Priority ─────────────────────────────────────────────────────────

        @Test
        @DisplayName("devePersistirERetornarPrioridadeAltoCorretamente")
        void devePersistirERetornarPrioridadeAltoCorretamente() {
            TodoList entidade = tarefaComPrioridade(Priority.ALTO, Category.TRABALHO);
            when(repository.save(any(TodoList.class))).thenReturn(entidade);

            TodoListResponseDTO resultado = service.create(
                    new TodoListRequestDTO("Urgente", null, null, Priority.ALTO, Category.TRABALHO));

            assertThat(resultado.priority()).isEqualTo(Priority.ALTO);
        }

        @Test
        @DisplayName("devePersistirERetornarPrioridadeMedioCorretamente")
        void devePersistirERetornarPrioridadeMedioCorretamente() {
            TodoList entidade = tarefaComPrioridade(Priority.MEDIO, Category.ESTUDO);
            when(repository.save(any(TodoList.class))).thenReturn(entidade);

            TodoListResponseDTO resultado = service.create(
                    new TodoListRequestDTO("Normal", null, null, Priority.MEDIO, Category.ESTUDO));

            assertThat(resultado.priority()).isEqualTo(Priority.MEDIO);
        }

        @Test
        @DisplayName("devePersistirERetornarPrioridadeBaixoCorretamente")
        void devePersistirERetornarPrioridadeBaixoCorretamente() {
            TodoList entidade = tarefaComPrioridade(Priority.BAIXO, Category.PESSOAL);
            when(repository.save(any(TodoList.class))).thenReturn(entidade);

            TodoListResponseDTO resultado = service.create(
                    new TodoListRequestDTO("Opcional", null, null, Priority.BAIXO, Category.PESSOAL));

            assertThat(resultado.priority()).isEqualTo(Priority.BAIXO);
        }

        // ── Category ─────────────────────────────────────────────────────────

        @Test
        @DisplayName("devePersistirERetornarCategoriaTrabalhoCorretamente")
        void devePersistirERetornarCategoriaTrabalhoCorretamente() {
            TodoList entidade = tarefaComPrioridade(Priority.ALTO, Category.TRABALHO);
            when(repository.save(any(TodoList.class))).thenReturn(entidade);

            TodoListResponseDTO resultado = service.create(
                    new TodoListRequestDTO("Reunião", null, null, Priority.ALTO, Category.TRABALHO));

            assertThat(resultado.category()).isEqualTo(Category.TRABALHO);
        }

        @Test
        @DisplayName("devePersistirERetornarCategoriaEstudoCorretamente")
        void devePersistirERetornarCategoriaEstudoCorretamente() {
            TodoList entidade = tarefaComPrioridade(Priority.MEDIO, Category.ESTUDO);
            when(repository.save(any(TodoList.class))).thenReturn(entidade);

            TodoListResponseDTO resultado = service.create(
                    new TodoListRequestDTO("Prova", null, null, Priority.MEDIO, Category.ESTUDO));

            assertThat(resultado.category()).isEqualTo(Category.ESTUDO);
        }

        @Test
        @DisplayName("devePersistirERetornarCategoriaPessoalCorretamente")
        void devePersistirERetornarCategoriaPessoalCorretamente() {
            TodoList entidade = tarefaComPrioridade(Priority.BAIXO, Category.PESSOAL);
            when(repository.save(any(TodoList.class))).thenReturn(entidade);

            TodoListResponseDTO resultado = service.create(
                    new TodoListRequestDTO("Academia", null, null, Priority.BAIXO, Category.PESSOAL));

            assertThat(resultado.category()).isEqualTo(Category.PESSOAL);
        }

        // ── Filtro por enum via findAll ────────────────────────────────────────

        @Test
        @DisplayName("deveFiltrarPorPrioridadeAltoViaFindAllERetornarTarefaCorrespondente")
        void deveFiltrarPorPrioridadeAltoViaFindAllERetornarTarefaCorrespondente() {
            when(repository.findAll(any(Specification.class))).thenReturn(List.of(tarefaFixture));

            List<TodoListResponseDTO> resultado = service.findAll(null, null, null, null, Priority.ALTO, null, null);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).priority()).isEqualTo(Priority.ALTO);
        }

        @Test
        @DisplayName("deveFiltrarPorPrioridadeAltoViaFindAllERetornarListaVaziaQuandoNaoHaCorrespondencia")
        void deveFiltrarPorPrioridadeAltoViaFindAllERetornarListaVaziaQuandoNaoHaCorrespondencia() {
            when(repository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

            List<TodoListResponseDTO> resultado = service.findAll(null, null, null, null, Priority.ALTO, null, null);

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("deveFiltrarPorCategoriaEstudoViaFindAllERetornarTarefaCorrespondente")
        void deveFiltrarPorCategoriaEstudoViaFindAllERetornarTarefaCorrespondente() {
            when(repository.findAll(any(Specification.class))).thenReturn(List.of(tarefaFixture));

            List<TodoListResponseDTO> resultado = service.findAll(null, null, null, null, null, Category.ESTUDO, null);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).category()).isEqualTo(Category.ESTUDO);
        }

        @Test
        @DisplayName("deveFiltrarPorCategoriaEstudoViaFindAllERetornarListaVaziaQuandoNaoHaCorrespondencia")
        void deveFiltrarPorCategoriaEstudoViaFindAllERetornarListaVaziaQuandoNaoHaCorrespondencia() {
            when(repository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

            List<TodoListResponseDTO> resultado = service.findAll(null, null, null, null, null, Category.ESTUDO, null);

            assertThat(resultado).isEmpty();
        }

        // ── Helper interno ────────────────────────────────────────────────────

        private TodoList tarefaComPrioridade(Priority priority, Category category) {
            return TodoList.builder()
                    .id(1L)
                    .title("Tarefa")
                    .priority(priority)
                    .category(category)
                    .completed(false)
                    .build();
        }
    }
}
