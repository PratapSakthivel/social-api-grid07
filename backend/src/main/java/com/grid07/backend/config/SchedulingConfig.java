package com.grid07.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduling configuration class for enabling task scheduling in the application.
 * Enables Spring's @Scheduled annotation support for background tasks.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Configuration class to enable scheduling
    // Additional scheduling configuration can be added here if needed
}