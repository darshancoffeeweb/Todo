package com.darshan.post.repository;

import com.darshan.post.entity.Status;
import com.darshan.post.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByUserId(Long userId);

    List<Todo> findByUserIdAndStatus(Long userId, Status status);

    Optional<Todo> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
