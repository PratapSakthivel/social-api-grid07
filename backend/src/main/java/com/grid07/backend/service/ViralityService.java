package com.grid07.backend.service;

import com.grid07.backend.constants.RedisKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service for managing virality scores and atomic guardrails using Redis.
 * Implements horizontal caps (bot count limits), vertical caps (depth limits), 
 * and cooldown mechanisms to prevent spam and ensure fair interaction.
 */
@Service
public class ViralityService {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
     * Increments virality score for bot replies (+1 point).
     * Called when a bot comments on a post.
     * 
     * @param postId the post ID
     */
    public void incrementViralityForBotReply(Long postId) {
        redisTemplate.opsForValue().increment(RedisKeys.viralityScore(postId), 1);
    }
    
    /**
     * Increments virality score for human likes (+20 points).
     * Called when a human user likes a post.
     * 
     * @param postId the post ID
     */
    public void incrementViralityForHumanLike(Long postId) {
        redisTemplate.opsForValue().increment(RedisKeys.viralityScore(postId), 20);
    }
    
    /**
     * Increments virality score for human comments (+50 points).
     * Called when a human user comments on a post.
     * 
     * @param postId the post ID
     */
    public void incrementViralityForHumanComment(Long postId) {
        redisTemplate.opsForValue().increment(RedisKeys.viralityScore(postId), 50);
    }
    
    /**
     * Atomically tries to increment bot count for a post.
     * Uses Redis INCR for atomic operation. If count exceeds 100, 
     * immediately rolls back with DECR and returns false.
     * 
     * @param postId the post ID
     * @return true if increment succeeded (count <= 100), false if limit exceeded
     */
    public boolean tryIncrementBotCount(Long postId) {
        String key = RedisKeys.botCount(postId);
        Long newCount = redisTemplate.opsForValue().increment(key, 1);
        
        if (newCount > 100) {
            // Rollback the increment - we exceeded the limit
            redisTemplate.opsForValue().decrement(key);
            return false;
        }
        
        return true;
    }
    
    /**
     * Decrements bot count for a post.
     * Used for rollback scenarios when other guardrails fail after bot count increment.
     * 
     * @param postId the post ID
     */
    public void decrementBotCount(Long postId) {
        redisTemplate.opsForValue().decrement(RedisKeys.botCount(postId));
    }
    
    /**
     * Checks if comment depth is within allowed limits.
     * Vertical cap: maximum depth of 20 levels.
     * 
     * @param depthLevel the comment depth level
     * @return true if depth <= 20, false otherwise
     */
    public boolean isDepthAllowed(int depthLevel) {
        return depthLevel <= 20;
    }
    
    /**
     * Atomically checks and sets cooldown for bot-to-human interactions.
     * Uses Redis SET NX EX for atomic operation with 10-minute expiry.
     * 
     * @param botId the bot ID
     * @param humanId the human user ID
     * @return true if cooldown was newly set (interaction allowed), 
     *         false if cooldown already exists (interaction blocked)
     */
    public boolean checkAndSetCooldown(Long botId, Long humanId) {
        String key = RedisKeys.cooldown(botId, humanId);
        // SET key value NX EX 600 - atomic operation
        // Returns true if key was newly set, false if key already existed
        return Boolean.TRUE.equals(
            redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofMinutes(10))
        );
    }
}