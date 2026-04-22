package com.grid07.backend.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Scheduler for sweeping and processing pending notifications.
 * Runs every 5 minutes to batch and send accumulated notifications.
 */
@Component
public class NotificationScheduler {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
     * Sweeps pending notifications and sends summarized batch notifications.
     * Runs every 5 minutes (300000 milliseconds).
     * For each user with pending notifications, sends a single summarized notification
     * and clears the pending list.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void sweepPendingNotifications() {
        System.out.println("[SWEEPER] Running notification sweep at " + LocalDateTime.now());
        
        // Scan Redis for all keys matching pattern "user:*:pending_notifs"
        Set<String> keys = redisTemplate.keys("user:*:pending_notifs");
        
        if (keys == null || keys.isEmpty()) {
            System.out.println("[SWEEPER] No pending notifications found.");
            return;
        }
        
        // Process each user's pending notifications
        for (String key : keys) {
            // Extract userId from key (format: "user:{userId}:pending_notifs")
            String[] parts = key.split(":");
            if (parts.length < 2) {
                continue;
            }
            String userId = parts[1];
            
            // Get all messages from the list
            List<String> pending = redisTemplate.opsForList().range(key, 0, -1);
            
            if (pending == null || pending.isEmpty()) {
                continue;
            }
            
            int count = pending.size();
            
            // Extract first bot ID from first message (format: "Bot {botId} replied...")
            String firstMessage = pending.get(0);
            String[] messageParts = firstMessage.split(" ");
            if (messageParts.length < 2) {
                continue;
            }
            String firstBot = messageParts[1];
            
            // Send summarized notification
            if (count == 1) {
                System.out.println("[SWEEPER] Summarized Push Notification to User " + userId + 
                    ": Bot " + firstBot + " interacted with your posts.");
            } else {
                System.out.println("[SWEEPER] Summarized Push Notification to User " + userId + 
                    ": Bot " + firstBot + " and " + (count - 1) + " others interacted with your posts.");
            }
            
            // Delete the key after processing
            redisTemplate.delete(key);
        }
    }
}