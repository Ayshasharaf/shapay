package com.shapay.webhook.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test-merchant")
public class FailingWebhookController {

    @PostMapping("/webhook-failure")
    public ResponseEntity<String> receiveWebhook(
        @RequestBody String payload
    ) {
        System.out.println("MERCHANT RECEIVED WEBHOOK (SIMULATED FAILURE):");
        System.out.println(payload);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Simulated server error");
    }
}