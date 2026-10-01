package com.darshan.auth.security;

import com.darshan.auth.dto.AuthResponse;
import com.darshan.auth.entity.Status;
import com.darshan.auth.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("register endpoint is publicly accessible without a token")
    void registerIsPubliclyAccessible() throws Exception {
        when(userService.register(any()))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(authResponse()));

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{\"name\":\"John\",\"email\":\"j@e.com\",\"password\":\"s\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("login endpoint is publicly accessible without a token")
    void loginIsPubliclyAccessible() throws Exception {
        when(userService.login(any())).thenReturn(ResponseEntity.ok(authResponse()));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"j@e.com\",\"password\":\"s\"}"))
                .andExpect(status().isOk());
    }

    private static AuthResponse authResponse() {
        return new AuthResponse(1L, "John", "j@e.com", Status.ACTIVE,
                Instant.parse("2024-01-01T00:00:00Z"), "token");
    }

    @Test
    @DisplayName("non-public endpoint rejects unauthenticated requests")
    void protectedEndpointRejectsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/protected"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("valid token passes the security filter")
    void validTokenPassesFilter() throws Exception {
        String token = jwtService.generateToken(1L, "john@example.com");

        // Passes authentication (filter sets principal); route is unmapped so 404, not 403.
        mockMvc.perform(get("/api/auth/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("invalid token is rejected")
    void invalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/auth/protected")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isForbidden());
    }
}
