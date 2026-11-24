package com.shapay.webhook.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shapay.webhook.service.WebhookEndpointEventService;
import com.shapay.webhook.service.WebhookEndpointService;

@RestController 
@RequestMapping("api/webhook-endpoints")
public class WebhookEndpointController {
    private final WebhookEndpointService webhookEndpointService;
    private final WebhookEndpointEventService webhookEndpointEventService;


    public WebhookEndpointController(WebhookEndpointService webhookEndpointService, WebhookEndpointEventService webhookEndpointEventService) {
        this.webhookEndpointService = webhookEndpointService;
        this.webhookEndpointEventService = webhookEndpointEventService;
    }
    // Register a merchant's endpoint
    @PostMapping 
    @ResponseStatus(HttpStatus.CREATED)

    public String createWebhookEndpoint(@RequestParam String url) {
        // Logic to create a webhook endpoint
        return webhookEndpointService.createWebhookEndpoint(url).getSecretKey();
    }
    
    // Subscribe an endpoint to an event
    @PostMapping("/{endpointId}/subscriptions")
    @ResponseStatus(HttpStatus.CREATED)
    public String subscribeToEvent(@PathVariable long endpointId, @RequestParam String eventType) {
        // Logic to subscribe to an event for a specific webhook endpoint
        webhookEndpointEventService.subscribe(endpointId, eventType);
        return "Successfully subscribed to " + eventType;


    }
}
