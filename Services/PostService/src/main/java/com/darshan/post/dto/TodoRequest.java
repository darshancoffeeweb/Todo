package com.darshan.post.dto;

import com.darshan.post.entity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// TodoRequest — only client-supplied fields
public record TodoRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 100)
        String title,

        @Size(max = 25)
        String description,

        Status status          // optional; default PENDING if null
) {
}
