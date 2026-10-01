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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repo;

    @Mock
    private UserMappper mapper;

    @Mock
    private JwtService jwtService;

    private UserService userService;

    private User savedUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(repo, mapper, jwtService);

        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("John Doe");
        savedUser.setEmail("john@example.com");
        savedUser.setPassword("secret");
        savedUser.setStatus(Status.ACTIVE);
        savedUser.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("creates user and returns CREATED with token")
        void createsUserAndReturnsCreatedWithToken() {
            UserRequest request = new UserRequest("John Doe", "john@example.com", "secret");
            AuthResponse authResponse = new AuthResponse(
                    1L, "John Doe", "john@example.com", Status.ACTIVE,
                    savedUser.getCreatedAt(), "token-123");

            when(repo.existsByEmail("john@example.com")).thenReturn(false);
            when(mapper.toEntity(request)).thenReturn(savedUser);
            when(repo.save(savedUser)).thenReturn(savedUser);
            when(jwtService.generateToken(1L, "john@example.com")).thenReturn("token-123");
            when(mapper.toAuthDto(savedUser, "token-123")).thenReturn(authResponse);

            ResponseEntity<AuthResponse> result = userService.register(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isEqualTo(authResponse);

            verify(repo).existsByEmail("john@example.com");
            verify(mapper).toEntity(request);
            verify(repo).save(savedUser);
            verify(jwtService).generateToken(1L, "john@example.com");
            verify(mapper).toAuthDto(savedUser, "token-123");
        }

        @Test
        @DisplayName("throws EmailAlreadyExistsException when email is taken")
        void throwsWhenEmailAlreadyExists() {
            UserRequest request = new UserRequest("John Doe", "john@example.com", "secret");

            when(repo.existsByEmail("john@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("john@example.com");

            verify(repo).existsByEmail("john@example.com");
            verify(repo, never()).save(any(User.class));
            verify(jwtService, never()).generateToken(anyLong(), anyString());
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("returns OK with token when credentials are valid")
        void returnsOkWhenCredentialsAreValid() {
            LoginRequest request = new LoginRequest("john@example.com", "secret");
            AuthResponse authResponse = new AuthResponse(
                    1L, "John Doe", "john@example.com", Status.ACTIVE,
                    savedUser.getCreatedAt(), "token-123");

            when(repo.findByEmail("john@example.com")).thenReturn(Optional.of(savedUser));
            when(jwtService.generateToken(1L, "john@example.com")).thenReturn("token-123");
            when(mapper.toAuthDto(savedUser, "token-123")).thenReturn(authResponse);

            ResponseEntity<AuthResponse> result = userService.login(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEqualTo(authResponse);

            verify(repo).findByEmail("john@example.com");
            verify(jwtService).generateToken(1L, "john@example.com");
            verify(mapper).toAuthDto(savedUser, "token-123");
        }

        @Test
        @DisplayName("throws InvalidCredentialsException when user is not found")
        void throwsWhenUserNotFound() {
            LoginRequest request = new LoginRequest("missing@example.com", "secret");

            when(repo.findByEmail("missing@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(repo).findByEmail("missing@example.com");
            verify(jwtService, never()).generateToken(anyLong(), anyString());
        }

        @Test
        @DisplayName("throws InvalidCredentialsException when password does not match")
        void throwsWhenPasswordDoesNotMatch() {
            LoginRequest request = new LoginRequest("john@example.com", "wrong-password");

            when(repo.findByEmail("john@example.com")).thenReturn(Optional.of(savedUser));

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(repo).findByEmail("john@example.com");
            verify(jwtService, never()).generateToken(anyLong(), anyString());
        }
    }
}
