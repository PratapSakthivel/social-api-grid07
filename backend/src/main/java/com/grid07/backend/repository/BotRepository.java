package com.grid07.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grid07.backend.entity.Bot;

/**
 * Repository interface for Bot entity operations.
 */
@Repository
public interface BotRepository extends JpaRepository<Bot, Long> {
}