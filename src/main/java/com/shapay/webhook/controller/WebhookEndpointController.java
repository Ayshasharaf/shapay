package com.shapay.webhook.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shapay.webhook.service.WebhookEndpointService;

@RestController 
@RequestMapping("api/webhook-endpoints")
public class WebhookEndpointController {
    private final WebhookEndpointService webhookEndpointService;

    public WebhookEndpointController(WebhookEndpointService webhookEndpointService) {
        this.webhookEndpointService = webhookEndpointService;
    }

    @PostMapping 
    @ResponseStatus(HttpStatus.CREATED)

    public String createWebhookEndpoint(@RequestParam String url) {
        // Logic to create a webhook endpoint
        return webhookEndpointService.createWebhookEndpoint(url).getSecretKey();
    }

}
