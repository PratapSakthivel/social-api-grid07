package com.grid07.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for adding a comment to a post.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentRequest {
    
    @NotNull
    private Long authorId;
    
    @NotBlank
    private String authorType;
    
    @NotBlank
    private String content;
    
    private int depthLevel = 0;
}