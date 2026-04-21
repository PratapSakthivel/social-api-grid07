package com.grid07.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration class for setting up Redis template and serialization.
 * Configures Redis connection and provides RedisTemplate bean for caching operations.
 */
@Configuration
public class RedisConfig {

    /**
     * Configures RedisTemplate with String serialization for keys and default serialization for values.
     * Uses String serializer for keys and default JDK serialization for values.
     *
     * @param connectionFactory Redis connection factory
     * @return Configured RedisTemplate instance
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use default serialization for values (JDK serialization)
        // This is the recommended approach in Spring Boot 4.0
        
        template.afterPropertiesSet();
        return template;
    }
}