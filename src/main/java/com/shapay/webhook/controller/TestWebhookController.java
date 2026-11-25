
package com.shapay.webhook.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shapay.common.service.WebhookSignatureService;

@RestController
@RequestMapping("/test-merchant")
public class TestWebhookController {

    private final WebhookSignatureService signatureService;

    @Value("${test-merchant.webhook-secret:}")
    private String secret;

    public TestWebhookController(
        WebhookSignatureService signatureService
    ) {
        this.signatureService = signatureService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveWebhook(
        @RequestBody String payload,
        @RequestHeader(
            value = "X-ShaPay-Signature",
            required = false
        ) String receivedSignature,
        @RequestParam(defaultValue = "false") boolean fail
    ) {

        if (secret.isBlank() || receivedSignature == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Missing webhook signature or secret");
        }

        boolean valid = signatureService.verifySignature(
            payload,
            secret,
            receivedSignature
        );

        if (!valid) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid webhook signature");
        }

        System.out.println("VALID WEBHOOK RECEIVED:");
        System.out.println(payload);

        if (fail) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Simulated server failure");
        }

        return ResponseEntity.ok("Webhook received");
    }
}
