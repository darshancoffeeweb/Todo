package com.darshan.post.controller;

import com.darshan.post.dto.TodoRequest;
import com.darshan.post.dto.TodoResponse;
import com.darshan.post.entity.Status;
import com.darshan.post.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    public ResponseEntity<TodoResponse> create(@Valid @RequestBody TodoRequest request,
                                               @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(todoService.create(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<TodoResponse>> getAll(@AuthenticationPrincipal Long userId,
                                                     @RequestParam(required = false) Status status) {
        if (status != null) {
            return ResponseEntity.ok(todoService.getByStatus(userId, status));
        }
        return ResponseEntity.ok(todoService.getAll(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getById(@PathVariable Long id,
                                                @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(todoService.getById(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody TodoRequest request,
                                               @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(todoService.update(id, request, userId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TodoResponse> patch(@PathVariable Long id,
                                              @RequestBody TodoRequest request,
                                              @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(todoService.patch(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                        @AuthenticationPrincipal Long userId) {
        todoService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
