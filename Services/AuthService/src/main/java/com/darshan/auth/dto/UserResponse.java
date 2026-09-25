package com.darshan.auth.dto;

import com.darshan.auth.entity.Status;

import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String email,
        Status status,
        Instant createdAt
) {
}
