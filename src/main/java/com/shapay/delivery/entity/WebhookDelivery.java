
package com.shapay.delivery.entity;

import java.time.LocalDateTime;

import com.shapay.webhook.entity.WebhookEndpoint;
import com.shapay.webhook.entity.WebhookEvent;

import jakarta.persistence.*;

@Entity
@Table(
    name = "webhook_deliveries",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_event_endpoint",
            columnNames = {"webhook_event_id", "endpoint_id"}
        )
    }
)
public class WebhookDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_event_id", nullable = false)
    private WebhookEvent webhookEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", nullable = false)
    private WebhookEndpoint endpoint;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public WebhookDelivery() {}

    public WebhookDelivery(WebhookEvent webhookEvent, WebhookEndpoint endpoint) {
        this.webhookEvent = webhookEvent;
        this.endpoint = endpoint;
    }
    public void incrementAttemptCount() {
    this.attemptCount++;
    }
    public void scheduleRetry(long delayInSeconds) {
    this.status = "RETRY";
    this.nextAttemptAt = LocalDateTime.now().plusSeconds(delayInSeconds);
}
    public void resetForReplay() {
        this.status = "PENDING";
        this.nextAttemptAt = null;
        this.claimedAt = null;
    }

    public Long getId() {
        return id;
    }

    public WebhookEvent getWebhookEvent() {
        return webhookEvent;
    }

    public void setWebhookEvent(WebhookEvent webhookEvent) {
        this.webhookEvent = webhookEvent;
    }

    public WebhookEndpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(WebhookEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public LocalDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public void setNextAttemptAt(LocalDateTime nextAttemptAt) {
        this.nextAttemptAt = nextAttemptAt;
    }

    public LocalDateTime getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(LocalDateTime claimedAt) {
        this.claimedAt = claimedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
