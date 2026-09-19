package com.darshan.todoapp.service;

import com.darshan.todoapp.dto.TodoRequest;
import com.darshan.todoapp.dto.TodoResponse;
import com.darshan.todoapp.entity.Todo;
import com.darshan.todoapp.repository.TodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<TodoResponse> findAll() {
        return todoRepository.findAll().stream()
                .map(TodoResponse::new)
                .toList();
    }

    public List<TodoResponse> findByCompleted(boolean completed) {
        return todoRepository.findByCompleted(completed).stream()
                .map(TodoResponse::new)
                .toList();
    }

    public TodoResponse findById(Long id) {
        return new TodoResponse(getEntity(id));
    }

    public TodoResponse create(TodoRequest request) {
        Todo todo = new Todo(request.getTitle(), request.getDescription());
        todo.setCompleted(Boolean.TRUE.equals(request.getCompleted()));
        return new TodoResponse(todoRepository.save(todo));
    }

    public TodoResponse update(Long id, TodoRequest request) {
        Todo todo = getEntity(id);
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setCompleted(Boolean.TRUE.equals(request.getCompleted()));
        return new TodoResponse(todoRepository.save(todo));
    }

    public TodoResponse patch(Long id, TodoRequest request) {
        Todo todo = getEntity(id);
        if (request.getTitle() != null) {
            todo.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            todo.setDescription(request.getDescription());
        }
        if (request.getCompleted() != null) {
            todo.setCompleted(request.getCompleted());
        }
        return new TodoResponse(todoRepository.save(todo));
    }

    public void delete(Long id) {
        Todo todo = getEntity(id);
        todoRepository.delete(todo);
    }

    private Todo getEntity(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }
}
