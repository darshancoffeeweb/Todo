package com.darshan.post.security;

import com.darshan.post.service.TodoService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    private static final String SECRET =
            "6c2b8f8e9a4d7f1e3b5c8a2d4f6e8a1c3b5d7f9e2a4c6b8d1f3e5a7c9b2d4f6e8a1c";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TodoService todoService;

    private SecretKey key;

    @BeforeEach
    void setUp() {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }

    private String token(Long userId, String email, long expirationMillis) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("protected endpoint rejects unauthenticated requests")
    void protectedEndpointRejectsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("valid token grants access")
    void validTokenGrantsAccess() throws Exception {
        String token = token(10L, "john@example.com", 60_000L);

        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("invalid token is rejected")
    void invalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("expired token is rejected")
    void expiredTokenIsRejected() throws Exception {
        String token = token(10L, "john@example.com", -60_000L);

        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
