package com.grid07.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new post.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {
    
    @NotNull
    private Long authorId;
    
    @NotBlank
    private String authorType;
    
    @NotBlank
    private String content;
}