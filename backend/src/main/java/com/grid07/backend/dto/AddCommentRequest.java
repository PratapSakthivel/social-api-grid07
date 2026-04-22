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
    
    // Explicit getters for IDE compatibility
    public Long getAuthorId() { return authorId; }
    public String getAuthorType() { return authorType; }
    public String getContent() { return content; }
    public int getDepthLevel() { return depthLevel; }
    
    // Explicit setters for IDE compatibility
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public void setAuthorType(String authorType) { this.authorType = authorType; }
    public void setContent(String content) { this.content = content; }
    public void setDepthLevel(int depthLevel) { this.depthLevel = depthLevel; }
}