package com.shapay.webhook.service;

import org.springframework.stereotype.Service;

import com.shapay.webhook.entity.WebhookEndpoint;
import com.shapay.webhook.entity.WebhookEndpointEvent;
import com.shapay.webhook.repository.WebhookEndpointEventRepository;
import com.shapay.webhook.repository.WebhookEndpointRepository;

import jakarta.transaction.Transactional;

@Service 
public class WebhookEndpointEventService {
    private final WebhookEndpointEventRepository subscriptionRepository;
     private final WebhookEndpointRepository endpointRepository;

    public WebhookEndpointEventService(WebhookEndpointEventRepository subscriptionRepository, WebhookEndpointRepository endpointRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.endpointRepository = endpointRepository;   
    }
    @Transactional 
    public WebhookEndpointEvent subscribe(long endpointId, String eventType) {
        // Check if the endpoint exists
        WebhookEndpoint endpoint = endpointRepository.findById(endpointId)
            .orElseThrow(() -> new RuntimeException("Endpoint not found"));
         // 2. Create subscription
        WebhookEndpointEvent subscription =
            new WebhookEndpointEvent(endpoint, eventType);

        // Save the subscription to the database
        return subscriptionRepository.save(subscription);
    }



    
}
