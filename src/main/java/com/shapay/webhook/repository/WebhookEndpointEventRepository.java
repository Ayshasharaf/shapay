package com.shapay.webhook.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shapay.webhook.entity.WebhookEndpointEvent;

public interface WebhookEndpointEventRepository extends JpaRepository<WebhookEndpointEvent, Long> {
    // Repository methods for WebhookEndpointEvent entity
    
}
