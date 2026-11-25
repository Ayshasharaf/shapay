package com.shapay.delivery.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.shapay.delivery.entity.WebhookDelivery;
import com.shapay.delivery.service.WebhookReplayService;

@RestController
@RequestMapping("/api/deliveries")
public class WebhookReplayController {

    private final WebhookReplayService replayService;

    public WebhookReplayController(WebhookReplayService replayService) {
        this.replayService = replayService;
    }

   @PostMapping("/{id}/replay")
    public ResponseEntity<Void> replay(@PathVariable Long id) {
        replayService.replay(id);
        return ResponseEntity.accepted().build();
    }

}