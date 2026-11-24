package com.shapay.webhook.service;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.shapay.webhook.entity.WebhookEndpoint;
import com.shapay.webhook.repository.WebhookEndpointRepository;

import jakarta.transaction.Transactional;

@Service 
public class WebhookEndpointService {

    private final WebhookEndpointRepository webhookEndpointRepository;
    public WebhookEndpointService(WebhookEndpointRepository webhookEndpointRepository) {
        this.webhookEndpointRepository = webhookEndpointRepository;
    }

    @Transactional 
    public WebhookEndpoint createWebhookEndpoint(String url) {
        // Generate a secure random secret

        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);

         String secretKey = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(randomBytes);

        // Create endpoint
        WebhookEndpoint endpoint = new WebhookEndpoint(
            url,
            secretKey,
            "ACTIVE"
        );
        // Save endpoint to the database
        return webhookEndpointRepository.save(endpoint);
    }
    
}
