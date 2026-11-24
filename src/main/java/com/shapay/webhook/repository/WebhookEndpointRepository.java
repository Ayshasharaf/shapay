package com.shapay.webhook.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shapay.webhook.entity.WebhookEndpoint;

public interface WebhookEndpointRepository  extends JpaRepository<WebhookEndpoint, Long> {
    // Repository methods for WebhookEndpoint entity
    
}
