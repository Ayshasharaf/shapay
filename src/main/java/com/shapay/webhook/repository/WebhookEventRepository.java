package com.shapay.webhook.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shapay.webhook.entity.WebhookEvent;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    // Repository methods for WebhookEvent entity
    
}
