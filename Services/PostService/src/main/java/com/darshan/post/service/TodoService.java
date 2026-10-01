package com.darshan.post.service;

import com.darshan.post.dto.TodoRequest;
import com.darshan.post.dto.TodoResponse;
import com.darshan.post.entity.Status;
import com.darshan.post.entity.Todo;
import com.darshan.post.exception.TodoNotFoundException;
import com.darshan.post.mapper.TodoMapper;
import com.darshan.post.repository.TodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TodoService {

    private final TodoRepository repo;
    private final TodoMapper mapper;

    public TodoService(TodoRepository repo, TodoMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public TodoResponse create(TodoRequest request, Long userId) {
        Todo todo = mapper.toEntity(request, userId);
        return mapper.toDto(repo.save(todo));
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> getAll(Long userId) {
        return repo.findByUserId(userId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> getByStatus(Long userId, Status status) {
        return repo.findByUserIdAndStatus(userId, status).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TodoResponse getById(Long id, Long userId) {
        return repo.findByIdAndUserId(id, userId)
                .map(mapper::toDto)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }

    public TodoResponse update(Long id, TodoRequest request, Long userId) {
        Todo todo = repo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TodoNotFoundException(id));

        todo.setTitle(request.title());
        todo.setDescription(request.description());
        if (request.status() != null) {
            todo.setStatus(request.status());
        }

        return mapper.toDto(repo.save(todo));
    }

    public TodoResponse patch(Long id, TodoRequest request, Long userId) {
        Todo todo = repo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TodoNotFoundException(id));

        if (request.title() != null) {
            todo.setTitle(request.title());
        }
        if (request.description() != null) {
            todo.setDescription(request.description());
        }
        if (request.status() != null) {
            todo.setStatus(request.status());
        }

        return mapper.toDto(repo.save(todo));
    }

    public void delete(Long id, Long userId) {
        if (!repo.existsByIdAndUserId(id, userId)) {
            throw new TodoNotFoundException(id);
        }
        repo.deleteById(id);
    }
}
