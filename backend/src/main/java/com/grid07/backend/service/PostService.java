package com.grid07.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Service for handling post-related operations with virality engine and atomic guardrails.
 */
@Service
public class PostService {
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private ViralityService viralityService;
    
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
     * Add a comment to a post with atomic guardrails.
     * Implements vertical cap (depth <= 20), horizontal cap (bot count <= 100), 
     * and cooldown mechanism (10 minutes between bot-human interactions).
     * 
     * @param postId the ID of the post
     * @param request the add comment request
     * @return ResponseEntity with the created comment and 201 status, or 429 if guardrails fail
     */
    public ResponseEntity<?> addComment(Long postId, AddCommentRequest request) {
        // Find post by ID, throw 404 if not found
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        
        AuthorType authorType = AuthorType.valueOf(request.getAuthorType());
        
        if (authorType == AuthorType.BOT) {
            // STEP 1 - Vertical Cap check: depth <= 20
            if (!viralityService.isDepthAllowed(request.getDepthLevel())) {
                return ResponseEntity.status(429).body("Rejected: Comment depth exceeds 20 levels");
            }
            
            // STEP 2 - Horizontal Cap check (Redis INCR atomic): bot count <= 100
            if (!viralityService.tryIncrementBotCount(postId)) {
                return ResponseEntity.status(429).body("Rejected: Post has reached 100 bot reply limit");
            }
            
            // STEP 3 - Cooldown Cap check (only if post author is USER)
            if (post.getAuthorType() == AuthorType.USER) {
                if (!viralityService.checkAndSetCooldown(request.getAuthorId(), post.getAuthorId())) {
                    // Rollback step 2
                    viralityService.decrementBotCount(postId);
                    return ResponseEntity.status(429).body("Rejected: Cooldown active for 10 minutes");
                }
            }
            
            // STEP 4 - All guardrails passed → NOW save to DB
            Comment savedComment = commentRepository.save(buildComment(postId, request));
            
            // Update virality score for bot reply
            viralityService.incrementViralityForBotReply(postId);
            
            // Send notification if post author is USER
            if (post.getAuthorType() == AuthorType.USER) {
                notificationService.handleBotInteraction(post.getAuthorId(), request.getAuthorId(), postId);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
            
        } else { // AuthorType.USER
            // Save comment directly (no guardrails for human users)
            Comment savedComment = commentRepository.save(buildComment(postId, request));
            
            // Update virality score for human comment
            viralityService.incrementViralityForHumanComment(postId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
        }
    }
    
    /**
     * Helper method to build Comment entity from request.
     * 
     * @param postId the post ID
     * @param request the add comment request
     * @return Comment entity
     */
    private Comment buildComment(Long postId, AddCommentRequest request) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(request.getAuthorId());
        comment.setAuthorType(AuthorType.valueOf(request.getAuthorType()));
        comment.setContent(request.getContent());
        comment.setDepthLevel(request.getDepthLevel());
        return comment;
    }
    
    /**
     * Like a post and update virality score.
     * 
     * @param postId the ID of the post to like
     * @param userId the ID of the user liking the post
     */
    public void likePost(Long postId, Long userId) {
        // Find post by ID, throw 404 if not found
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        
        // Update virality score for human like
        viralityService.incrementViralityForHumanLike(postId);
        
        System.out.println("User " + userId + " liked post " + postId);
    }
}