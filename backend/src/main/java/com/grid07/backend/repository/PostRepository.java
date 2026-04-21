package com.grid07.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grid07.backend.entity.Post;

/**
 * Repository interface for Post entity operations.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
}