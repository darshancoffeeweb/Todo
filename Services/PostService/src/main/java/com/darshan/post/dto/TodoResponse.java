package com.darshan.post.dto;

import com.darshan.post.entity.Status;

import java.time.Instant;

// TodoResponse — what server returns
public record TodoResponse(
        Long id,
        Long userId,
        String title,
        String description,
        Status status,
        Instant createdAt,     // typo fixed
        Instant updatedAt
) {
}