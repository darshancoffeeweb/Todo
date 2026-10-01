package com.darshan.post.mapper;

import com.darshan.post.dto.TodoRequest;
import com.darshan.post.dto.TodoResponse;
import com.darshan.post.entity.Status;
import com.darshan.post.entity.Todo;
import org.springframework.stereotype.Component;

@Component
public class TodoMapper {

    public Todo toEntity(TodoRequest request, Long userId) {
        Todo todo = new Todo();
        todo.setUserId(userId);
        todo.setTitle(request.title());
        todo.setDescription(request.description());
        todo.setStatus(request.status() == null ? Status.PENDING : request.status());
        return todo;
    }

    public TodoResponse toDto(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getUserId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.getStatus(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
        );
    }
}