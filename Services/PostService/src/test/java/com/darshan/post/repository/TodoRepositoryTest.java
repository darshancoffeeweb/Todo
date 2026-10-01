package com.darshan.post.repository;

import com.darshan.post.entity.Status;
import com.darshan.post.entity.Todo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TodoRepositoryTest {

    @Autowired
    private TodoRepository todoRepository;

    private Todo saveTodo(Long userId, String title, Status status) {
        Todo todo = new Todo();
        todo.setUserId(userId);
        todo.setTitle(title);
        todo.setDescription("description");
        todo.setStatus(status);
        return todoRepository.save(todo);
    }

    @Test
    @DisplayName("findByUserId returns todos belonging to the user")
    void findByUserIdReturnsTodosForUser() {
        saveTodo(10L, "Task A", Status.PENDING);
        saveTodo(10L, "Task B", Status.COMPLETED);
        saveTodo(20L, "Other user task", Status.PENDING);

        List<Todo> result = todoRepository.findByUserId(10L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Todo::getTitle).containsExactlyInAnyOrder("Task A", "Task B");
    }

    @Test
    @DisplayName("findByUserId returns empty list when user has no todos")
    void findByUserIdReturnsEmptyWhenNone() {
        List<Todo> result = todoRepository.findByUserId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUserIdAndStatus filters by user and status")
    void findByUserIdAndStatusFiltersCorrectly() {
        saveTodo(10L, "Pending task", Status.PENDING);
        saveTodo(10L, "Done task", Status.COMPLETED);
        saveTodo(20L, "Other pending", Status.PENDING);

        List<Todo> result = todoRepository.findByUserIdAndStatus(10L, Status.PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Pending task");
    }

    @Test
    @DisplayName("findByUserIdAndStatus returns empty when no match")
    void findByUserIdAndStatusReturnsEmptyWhenNoMatch() {
        saveTodo(10L, "Pending task", Status.PENDING);

        List<Todo> result = todoRepository.findByUserIdAndStatus(10L, Status.IN_PROGRESS);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndUserId returns the todo when it belongs to the user")
    void findByIdAndUserIdReturnsTodoWhenMatches() {
        Todo saved = saveTodo(10L, "Task A", Status.PENDING);

        Optional<Todo> result = todoRepository.findByIdAndUserId(saved.getId(), 10L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Task A");
    }

    @Test
    @DisplayName("findByIdAndUserId returns empty when the todo belongs to another user")
    void findByIdAndUserIdReturnsEmptyWhenWrongUser() {
        Todo saved = saveTodo(10L, "Task A", Status.PENDING);

        Optional<Todo> result = todoRepository.findByIdAndUserId(saved.getId(), 20L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByIdAndUserId returns true when id belongs to user")
    void existsByIdAndUserIdReturnsTrueWhenMatches() {
        Todo saved = saveTodo(10L, "Task A", Status.PENDING);

        assertThat(todoRepository.existsByIdAndUserId(saved.getId(), 10L)).isTrue();
    }

    @Test
    @DisplayName("existsByIdAndUserId returns false when id belongs to another user")
    void existsByIdAndUserIdReturnsFalseWhenWrongUser() {
        Todo saved = saveTodo(10L, "Task A", Status.PENDING);

        assertThat(todoRepository.existsByIdAndUserId(saved.getId(), 20L)).isFalse();
    }
}
