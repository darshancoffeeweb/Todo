package com.darshan.auth.service;

import com.darshan.auth.dto.AuthResponse;
import com.darshan.auth.dto.LoginRequest;
import com.darshan.auth.dto.UserRequest;
import com.darshan.auth.entity.Status;
import com.darshan.auth.entity.User;
import com.darshan.auth.exception.EmailAlreadyExistsException;
import com.darshan.auth.exception.InvalidCredentialsException;
import com.darshan.auth.mapper.UserMappper;
import com.darshan.auth.repository.UserRepository;
import com.darshan.auth.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository repo;
    private final UserMappper mapper;
    private final JwtService jwtService;

    public UserService(UserRepository repo, UserMappper mapper, JwtService jwtService) {
        this.repo = repo;
        this.mapper = mapper;
        this.jwtService = jwtService;
    }

    public ResponseEntity<AuthResponse> register(UserRequest request) {
        if (repo.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = mapper.toEntity(request);
        user.setStatus(Status.ACTIVE);
        User saved = repo.save(user);
        String token = jwtService.generateToken(saved.getId(), saved.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toAuthDto(saved, token));
    }

    public ResponseEntity<AuthResponse> login(LoginRequest request) {
        User user = repo.findByEmail(request.email())
                .filter(u -> u.getPassword().equals(request.password()))
                .orElseThrow(InvalidCredentialsException::new);

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return ResponseEntity.ok(mapper.toAuthDto(user, token));
    }
}
