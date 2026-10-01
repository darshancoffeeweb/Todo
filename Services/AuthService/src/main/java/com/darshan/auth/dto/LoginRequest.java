package com.darshan.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
