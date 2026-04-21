package com.grid07.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grid07.backend.dto.AddCommentRequest;
import com.grid07.backend.dto.CreatePostRequest;
import com.grid07.backend.entity.Comment;
import com.grid07.backend.entity.Post;
import com.grid07.backend.service.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for post-related operations.
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    
    private final PostService postService;
    
    /**
     * Create a new post.
     * 
     * @param request the create post request
     * @return ResponseEntity with the created post and 201 status
     */
    @PostMapping
    public ResponseEntity<Post> createPost(@Valid @RequestBody CreatePostRequest request) {
        Post createdPost = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }
    
    /**
     * Add a comment to a post.
     * 
     * @param postId the ID of the post
     * @param request the add comment request
     * @return ResponseEntity with the created comment and 201 status
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long postId, 
                                            @Valid @RequestBody AddCommentRequest request) {
        return postService.addComment(postId, request);
    }
    
    /**
     * Like a post.
     * 
     * @param postId the ID of the post to like
     * @param userId the ID of the user liking the post
     * @return ResponseEntity with success message and 200 status
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<String> likePost(@PathVariable Long postId, 
                                         @RequestParam Long userId) {
        postService.likePost(postId, userId);
        return ResponseEntity.ok("Post liked");
    }
}