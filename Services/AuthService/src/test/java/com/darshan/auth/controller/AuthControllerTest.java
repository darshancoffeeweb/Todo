package com.darshan.auth.controller;

import com.darshan.auth.dto.AuthResponse;
import com.darshan.auth.entity.Status;
import com.darshan.auth.exception.EmailAlreadyExistsException;
import com.darshan.auth.exception.InvalidCredentialsException;
import com.darshan.auth.security.JwtAuthenticationFilter;
import com.darshan.auth.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String REGISTER_URL = "/api/auth/register";
    private static final String LOGIN_URL = "/api/auth/login";

    private static AuthResponse authResponse() {
        return new AuthResponse(
                1L, "John Doe", "john@example.com", Status.ACTIVE,
                Instant.parse("2024-01-01T00:00:00Z"), "token-123");
    }

    @Test
    @DisplayName("register returns 201 with auth response")
    void registerReturnsCreatedWithBody() throws Exception {
        when(userService.register(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(authResponse()));

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.token").value("token-123"));

        verify(userService).register(any());
    }

    @Test
    @DisplayName("register returns 409 when email already exists")
    void registerReturnsConflictWhenEmailExists() throws Exception {
        when(userService.register(any()))
                .thenThrow(new EmailAlreadyExistsException("john@example.com"));

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isConflict());

        verify(userService).register(any());
    }

    @Test
    @DisplayName("login returns 200 with auth response")
    void loginReturnsOkWithBody() throws Exception {
        when(userService.login(any()))
                .thenReturn(ResponseEntity.ok(authResponse()));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.token").value("token-123"));

        verify(userService).login(any());
    }

    @Test
    @DisplayName("login returns 401 on invalid credentials")
    void loginReturnsUnauthorizedOnBadCredentials() throws Exception {
        when(userService.login(any()))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());

        verify(userService).login(any());
    }
}
