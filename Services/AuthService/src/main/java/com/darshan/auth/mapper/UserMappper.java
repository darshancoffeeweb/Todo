package com.darshan.auth.mapper;

import com.darshan.auth.dto.AuthResponse;
import com.darshan.auth.dto.UserRequest;
import com.darshan.auth.dto.UserResponse;
import com.darshan.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMappper {
    public User toEntity(UserRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password());
        return user;

    }

    public UserResponse toDto(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }

    public AuthResponse toAuthDto(User user, String token) {
        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getStatus(),
                user.getCreatedAt(),
                token
        );
    }
}
