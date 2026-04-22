package com.grid07.backend.constants;

/**
 * Redis key constants for the virality engine and atomic guardrails.
 * All keys follow a consistent naming pattern for easy debugging and monitoring.
 */
public class RedisKeys {
    
    /**
     * Key for tracking virality score of a post
     * @param postId the post ID
     * @return Redis key for virality score
     */
    public static String viralityScore(Long postId) { 
        return "post:" + postId + ":virality_score"; 
    }
    
    /**
     * Key for tracking bot comment count per post (horizontal cap)
     * @param postId the post ID
     * @return Redis key for bot count
     */
    public static String botCount(Long postId) { 
        return "post:" + postId + ":bot_count"; 
    }
    
    /**
     * Key for bot-to-human interaction cooldown (10 minutes)
     * @param botId the bot ID
     * @param humanId the human user ID
     * @return Redis key for cooldown
     */
    public static String cooldown(Long botId, Long humanId) { 
        return "cooldown:bot_" + botId + ":human_" + humanId; 
    }
    
    /**
     * Key for notification cooldown per user
     * @param userId the user ID
     * @return Redis key for notification cooldown
     */
    public static String notifCooldown(Long userId) { 
        return "notif:cooldown:user_" + userId; 
    }
    
    /**
     * Key for pending notifications queue per user
     * @param userId the user ID
     * @return Redis key for pending notifications
     */
    public static String pendingNotifs(Long userId) { 
        return "user:" + userId + ":pending_notifs"; 
    }
}