package com.darshan.post.controller;

import com.darshan.post.dto.TodoRequest;
import com.darshan.post.dto.TodoResponse;
import com.darshan.post.entity.Status;
import com.darshan.post.exception.TodoNotFoundException;
import com.darshan.post.service.TodoService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TodoControllerTest {

    private static final String SECRET =
            "6c2b8f8e9a4d7f1e3b5c8a2d4f6e8a1c3b5d7f9e2a4c6b8d1f3e5a7c9b2d4f6e8a1c";

    private static final String BASE_URL = "/api/todos";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TodoService todoService;

    private SecretKey key;
    private String authHeader;

    @BeforeEach
    void setUp() {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        authHeader = "Bearer " + Jwts.builder()
                .subject("john@example.com")
                .claim("userId", 10L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000L))
                .signWith(key)
                .compact();
    }

    private static TodoResponse todoResponse() {
        return new TodoResponse(
                1L, 10L, "Buy groceries", "Milk, eggs, bread", Status.PENDING,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"));
    }

    @Test
    @DisplayName("create returns 201 with the created todo")
    void createReturnsCreated() throws Exception {
        when(todoService.create(any(TodoRequest.class), eq(10L))).thenReturn(todoResponse());

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Buy groceries\",\"description\":\"Milk, eggs, bread\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Buy groceries"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(todoService).create(any(TodoRequest.class), eq(10L));
    }

    @Test
    @DisplayName("create returns 400 when title is blank")
    void createReturnsBadRequestWhenTitleBlank() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"desc\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getAll returns 200 with the list of todos")
    void getAllReturnsOkWithList() throws Exception {
        when(todoService.getAll(10L)).thenReturn(List.of(todoResponse()));

        mockMvc.perform(get(BASE_URL).header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Buy groceries"));

        verify(todoService).getAll(10L);
    }

    @Test
    @DisplayName("getAll with status filter delegates to getByStatus")
    void getAllWithStatusFilterDelegatesToGetByStatus() throws Exception {
        when(todoService.getByStatus(10L, Status.COMPLETED)).thenReturn(List.of(todoResponse()));

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", authHeader)
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk());

        verify(todoService).getByStatus(10L, Status.COMPLETED);
    }

    @Test
    @DisplayName("getById returns 200 with the todo")
    void getByIdReturnsOk() throws Exception {
        when(todoService.getById(1L, 10L)).thenReturn(todoResponse());

        mockMvc.perform(get(BASE_URL + "/1").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(todoService).getById(1L, 10L);
    }

    @Test
    @DisplayName("getById returns 404 when todo is not found")
    void getByIdReturnsNotFound() throws Exception {
        when(todoService.getById(99L, 10L)).thenThrow(new TodoNotFoundException(99L));

        mockMvc.perform(get(BASE_URL + "/99").header("Authorization", authHeader))
                .andExpect(status().isNotFound());

        verify(todoService).getById(99L, 10L);
    }

    @Test
    @DisplayName("update returns 200 with the updated todo")
    void updateReturnsOk() throws Exception {
        when(todoService.update(eq(1L), any(TodoRequest.class), eq(10L))).thenReturn(todoResponse());

        mockMvc.perform(put(BASE_URL + "/1")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated\",\"description\":\"New desc\",\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(todoService).update(eq(1L), any(TodoRequest.class), eq(10L));
    }

    @Test
    @DisplayName("update returns 400 when title is blank")
    void updateReturnsBadRequestWhenTitleBlank() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"desc\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("patch returns 200 with the patched todo")
    void patchReturnsOk() throws Exception {
        when(todoService.patch(eq(1L), any(TodoRequest.class), eq(10L))).thenReturn(todoResponse());

        mockMvc.perform(patch(BASE_URL + "/1")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(todoService).patch(eq(1L), any(TodoRequest.class), eq(10L));
    }

    @Test
    @DisplayName("delete returns 204 no content")
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1").header("Authorization", authHeader))
                .andExpect(status().isNoContent());

        verify(todoService).delete(1L, 10L);
    }

    @Test
    @DisplayName("delete returns 404 when todo is not found")
    void deleteReturnsNotFound() throws Exception {
        doThrow(new TodoNotFoundException(99L))
                .when(todoService).delete(99L, 10L);

        mockMvc.perform(delete(BASE_URL + "/99").header("Authorization", authHeader))
                .andExpect(status().isNotFound());

        verify(todoService).delete(99L, 10L);
    }
}
