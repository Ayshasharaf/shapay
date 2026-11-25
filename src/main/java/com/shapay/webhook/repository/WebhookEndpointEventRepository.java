package com.shapay.webhook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shapay.webhook.entity.WebhookEndpointEvent;

public interface WebhookEndpointEventRepository extends JpaRepository<WebhookEndpointEvent, Long> {
    // Repository methods for WebhookEndpointEvent entity
    List<WebhookEndpointEvent> findByEventType(String eventType);

    
}
