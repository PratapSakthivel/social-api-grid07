package com.grid07.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grid07.backend.entity.Comment;

/**
 * Repository interface for Comment entity operations.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * Find all comments for a specific post.
     * @param postId the ID of the post
     * @return list of comments for the post
     */
    List<Comment> findByPostId(Long postId);
}