package com.grid07.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.grid07.backend.dto.AddCommentRequest;
import com.grid07.backend.dto.CreatePostRequest;
import com.grid07.backend.entity.AuthorType;
import com.grid07.backend.entity.Comment;
import com.grid07.backend.entity.Post;
import com.grid07.backend.repository.CommentRepository;
import com.grid07.backend.repository.PostRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for handling post-related operations.
 */
@Service
@RequiredArgsConstructor
public class PostService {
    
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;
    
    /**
     * Create a new post.
     * 
     * @param request the create post request
     * @return the created post
     */
    public Post createPost(CreatePostRequest request) {
        Post post = new Post();
        post.setAuthorId(request.getAuthorId());
        post.setAuthorType(AuthorType.valueOf(request.getAuthorType()));
        post.setContent(request.getContent());
        
        return postRepository.save(post);
    }
    
    /**
     * Add a comment to a post.
     * 
     * @param postId the ID of the post
     * @param request the add comment request
     * @return ResponseEntity with the created comment and 201 status
     */
    public ResponseEntity<Comment> addComment(Long postId, AddCommentRequest request) {
        // Find post by ID, throw 404 if not found
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        
        // Build Comment
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(request.getAuthorId());
        comment.setAuthorType(AuthorType.valueOf(request.getAuthorType()));
        comment.setContent(request.getContent());
        comment.setDepthLevel(request.getDepthLevel());
        
        Comment savedComment = commentRepository.save(comment);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
    }
    
    /**
     * Like a post.
     * 
     * @param postId the ID of the post to like
     * @param userId the ID of the user liking the post
     */
    public void likePost(Long postId, Long userId) {
        // Find post by ID, throw 404 if not found
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        
        // Just print for now - NO Redis logic yet
        System.out.println("User " + userId + " liked post " + postId);
    }
}