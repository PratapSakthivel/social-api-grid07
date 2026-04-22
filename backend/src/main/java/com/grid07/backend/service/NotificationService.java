package com.grid07.backend.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.grid07.backend.constants.RedisKeys;

/**
 * Service for handling notifications with smart batching and throttling.
 * Implements 15-minute cooldown with Redis list buffering for pending notifications.
 */
@Service
public class NotificationService {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
     * Handle bot interaction notifications with smart throttling.
     * If no cooldown is active, sends immediate notification and sets 15-minute cooldown.
     * If cooldown is active, buffers notification in Redis list for later batch processing.
     * 
     * @param userId the ID of the user to notify
     * @param botId the ID of the bot that interacted
     * @param postId the ID of the post that was interacted with
     */
    public void handleBotInteraction(Long userId, Long botId, Long postId) {
        String cooldownKey = RedisKeys.notifCooldown(userId);
        String pendingKey = RedisKeys.pendingNotifs(userId);
        String message = "Bot " + botId + " replied to your post " + postId;
        
        // Check if cooldown key exists
        Boolean hasCooldown = redisTemplate.hasKey(cooldownKey);
        
        if (Boolean.FALSE.equals(hasCooldown)) {
            // No active cooldown - send immediate notification
            System.out.println("[NOTIF] Push Notification Sent to User " + userId + ": " + message);
            
            // Set cooldown key with 15 minute TTL
            redisTemplate.opsForValue().set(cooldownKey, "1", Duration.ofMinutes(15));
        } else {
            // Cooldown active - buffer notification
            redisTemplate.opsForList().rightPush(pendingKey, message);
            System.out.println("[NOTIF] Buffered notification for User " + userId + ": " + message);
        }
    }
}