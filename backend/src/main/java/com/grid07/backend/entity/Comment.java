package com.grid07.backend.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Comment entity representing comments on posts.
 */
@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "post_id", nullable = false)
    private Long postId;
    
    @Column(name = "author_id", nullable = false)
    private Long authorId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "author_type", nullable = false)
    private AuthorType authorType;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "depth_level", nullable = false)
    private int depthLevel = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Explicit getters for IDE compatibility
    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getAuthorId() { return authorId; }
    public AuthorType getAuthorType() { return authorType; }
    public String getContent() { return content; }
    public int getDepthLevel() { return depthLevel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // Explicit setters for IDE compatibility
    public void setId(Long id) { this.id = id; }
    public void setPostId(Long postId) { this.postId = postId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public void setAuthorType(AuthorType authorType) { this.authorType = authorType; }
    public void setContent(String content) { this.content = content; }
    public void setDepthLevel(int depthLevel) { this.depthLevel = depthLevel; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}