package com.darshan.post.service;

import com.darshan.post.dto.TodoRequest;
import com.darshan.post.dto.TodoResponse;
import com.darshan.post.entity.Status;
import com.darshan.post.entity.Todo;
import com.darshan.post.exception.TodoNotFoundException;
import com.darshan.post.mapper.TodoMapper;
import com.darshan.post.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class  TodoServiceTest {

    @Mock
    private TodoRepository repo;

    @Mock
    private TodoMapper mapper;

    private TodoService todoService;

    private Todo todo;
    private TodoResponse todoResponse;

    @BeforeEach
    void setUp() {
        todoService = new TodoService(repo, mapper);

        todo = new Todo();
        todo.setId(1L);
        todo.setUserId(10L);
        todo.setTitle("Buy groceries");
        todo.setDescription("Milk, eggs, bread");
        todo.setStatus(Status.PENDING);
        todo.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        todo.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));

        todoResponse = new TodoResponse(
                1L, 10L, "Buy groceries", "Milk, eggs, bread", Status.PENDING,
                todo.getCreatedAt(), todo.getUpdatedAt());
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates a todo and returns its response")
        void createsTodoAndReturnsResponse() {
            TodoRequest request = new TodoRequest("Buy groceries", "Milk, eggs, bread", Status.PENDING);

            when(mapper.toEntity(request, 10L)).thenReturn(todo);
            when(repo.save(todo)).thenReturn(todo);
            when(mapper.toDto(todo)).thenReturn(todoResponse);

            TodoResponse result = todoService.create(request, 10L);

            assertThat(result).isEqualTo(todoResponse);

            verify(mapper).toEntity(request, 10L);
            verify(repo).save(todo);
            verify(mapper).toDto(todo);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("returns mapped list of todos for the user")
        void returnsMappedList() {
            when(repo.findByUserId(10L)).thenReturn(List.of(todo));
            when(mapper.toDto(todo)).thenReturn(todoResponse);

            List<TodoResponse> result = todoService.getAll(10L);

            assertThat(result).containsExactly(todoResponse);

            verify(repo).findByUserId(10L);
            verify(mapper).toDto(todo);
        }

        @Test
        @DisplayName("returns empty list when user has no todos")
        void returnsEmptyListWhenNoTodos() {
            when(repo.findByUserId(10L)).thenReturn(List.of());

            List<TodoResponse> result = todoService.getAll(10L);

            assertThat(result).isEmpty();
            verify(repo).findByUserId(10L);
        }
    }

    @Nested
    @DisplayName("getByStatus")
    class GetByStatus {

        @Test
        @DisplayName("returns mapped list of todos matching the status")
        void returnsMappedListForStatus() {
            when(repo.findByUserIdAndStatus(10L, Status.PENDING)).thenReturn(List.of(todo));
            when(mapper.toDto(todo)).thenReturn(todoResponse);

            List<TodoResponse> result = todoService.getByStatus(10L, Status.PENDING);

            assertThat(result).containsExactly(todoResponse);

            verify(repo).findByUserIdAndStatus(10L, Status.PENDING);
            verify(mapper).toDto(todo);
        }

        @Test
        @DisplayName("returns empty list when no todos match the status")
        void returnsEmptyListWhenNoMatch() {
            when(repo.findByUserIdAndStatus(10L, Status.COMPLETED)).thenReturn(List.of());

            List<TodoResponse> result = todoService.getByStatus(10L, Status.COMPLETED);

            assertThat(result).isEmpty();
            verify(repo).findByUserIdAndStatus(10L, Status.COMPLETED);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns the todo when found")
        void returnsTodoWhenFound() {
            when(repo.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(todo));
            when(mapper.toDto(todo)).thenReturn(todoResponse);

            TodoResponse result = todoService.getById(1L, 10L);

            assertThat(result).isEqualTo(todoResponse);

            verify(repo).findByIdAndUserId(1L, 10L);
            verify(mapper).toDto(todo);
        }

        @Test
        @DisplayName("throws TodoNotFoundException when not found")
        void throwsWhenNotFound() {
            when(repo.findByIdAndUserId(99L, 10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> todoService.getById(99L, 10L))
                    .isInstanceOf(TodoNotFoundException.class)
                    .hasMessageContaining("99");

            verify(repo).findByIdAndUserId(99L, 10L);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates all fields and returns the response")
        void updatesAllFields() {
            TodoRequest request = new TodoRequest("New title", "New desc", Status.COMPLETED);

            when(repo.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(todo));
            when(repo.save(todo)).thenReturn(todo);
            when(mapper.toDto(todo)).thenReturn(todoResponse);

            TodoResponse result = todoService.update(1L, request, 10L);

            assertThat(result).isEqualTo(todoResponse);
            assertThat(todo.getTitle()).isEqualTo("New title");
            assertThat(todo.getDescription()).isEqualTo("New desc");
            assertThat(todo.getStatus()).isEqualTo(Status.COMPLETED);

            verify(repo).findByIdAndUserId(1L, 10L);
            verify(repo).save(todo);
            verify(mapper).toDto(todo);
        }

        @Test
        @DisplayName("keeps existing status when request status is null")
        void keepsExistingStatusWhenNull() {
            TodoRequest request = new TodoRequest("New title", "New desc", null);

            when(repo.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(todo));
            when(repo.save(todo)).thenReturn(todo);
            when(mapper.toDto(todo)).thenReturn(todoResponse);

            todoService.update(1L, request, 10L);

            assertThat(todo.getStatus()).isEqualTo(Status.PENDING);

            verify(repo).save(todo);
        }

        @Test
        @DisplayName("throws TodoNotFoundException when not found")
        void throwsWhenNotFound() {
            TodoRequest request = new TodoRequest("New title", "New desc", Status.COMPLETED);

            when(repo.findByIdAndUserId(99L, 10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> todoService.update(99L, request, 10L))
                    .isInstanceOf(TodoNotFoundException.class);

            verify(repo).findByIdAndUserId(99L, 10L);
            verify(repo, never()).save(any(Todo.class));
        }
    }

    @Nested
    @DisplayName("patch")
    class Patch {

        @Test
        @DisplayName("patches only provided fields")
        void patchesOnlyProvidedFields() {
            TodoRequest request = new TodoRequest("Patched title", null, null);

            when(repo.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(todo));
            when(repo.save(todo)).thenReturn(todo);
            when(mapper.toDto(todo)).thenReturn(todoResponse);

            TodoResponse result = todoService.patch(1L, request, 10L);

            assertThat(result).isEqualTo(todoResponse);
            assertThat(todo.getTitle()).isEqualTo("Patched title");
            assertThat(todo.getDescription()).isEqualTo("Milk, eggs, bread");
            assertThat(todo.getStatus()).isEqualTo(Status.PENDING);

            verify(repo).findByIdAndUserId(1L, 10L);
            verify(repo).save(todo);
        }

        @Test
        @DisplayName("patches all fields when provided")
        void patchesAllFieldsWhenProvided() {
            TodoRequest request = new TodoRequest("T", "D", Status.IN_PROGRESS);

            when(repo.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(todo));
            when(repo.save(todo)).thenReturn(todo);
            when(mapper.toDto(todo)).thenReturn(todoResponse);

            todoService.patch(1L, request, 10L);

            assertThat(todo.getTitle()).isEqualTo("T");
            assertThat(todo.getDescription()).isEqualTo("D");
            assertThat(todo.getStatus()).isEqualTo(Status.IN_PROGRESS);

            verify(repo).save(todo);
        }

        @Test
        @DisplayName("does not patch title when it is null")
        void doesNotPatchTitleWhenNull() {
            TodoRequest request = new TodoRequest(null, "New desc", Status.COMPLETED);

            when(repo.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(todo));
            when(repo.save(todo)).thenReturn(todo);
            when(mapper.toDto(todo)).thenReturn(todoResponse);

            todoService.patch(1L, request, 10L);

            assertThat(todo.getTitle()).isEqualTo("Buy groceries");
            assertThat(todo.getDescription()).isEqualTo("New desc");
            assertThat(todo.getStatus()).isEqualTo(Status.COMPLETED);

            verify(repo).save(todo);
        }

        @Test
        @DisplayName("throws TodoNotFoundException when not found")
        void throwsWhenNotFound() {
            TodoRequest request = new TodoRequest("T", null, null);

            when(repo.findByIdAndUserId(99L, 10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> todoService.patch(99L, request, 10L))
                    .isInstanceOf(TodoNotFoundException.class);

            verify(repo).findByIdAndUserId(99L, 10L);
            verify(repo, never()).save(any(Todo.class));
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes the todo when it exists")
        void deletesWhenExists() {
            when(repo.existsByIdAndUserId(1L, 10L)).thenReturn(true);

            todoService.delete(1L, 10L);

            verify(repo).existsByIdAndUserId(1L, 10L);
            verify(repo).deleteById(1L);
        }

        @Test
        @DisplayName("throws TodoNotFoundException when todo does not exist")
        void throwsWhenNotExists() {
            when(repo.existsByIdAndUserId(99L, 10L)).thenReturn(false);

            assertThatThrownBy(() -> todoService.delete(99L, 10L))
                    .isInstanceOf(TodoNotFoundException.class);

            verify(repo).existsByIdAndUserId(99L, 10L);
            verify(repo, never()).deleteById(anyLong());
        }
    }
}
