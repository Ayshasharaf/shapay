package com.shapay.webhook.entity;

import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


@Entity 
@Table (name="webhook_endpoint_events",uniqueConstraints = {
    @UniqueConstraint(
        name = "uq_endpoint_event",
            columnNames = {"endpoint_id", "event_type"}
    )
})
public class WebhookEndpointEvent {

    @Id  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", nullable = false)
    private WebhookEndpoint endpoint;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;
    public WebhookEndpointEvent() {
    }


     public WebhookEndpointEvent(
        WebhookEndpoint endpoint,
        String eventType
    ) {
        this.endpoint = endpoint;
        this.eventType = eventType;
    }
    public Long getId() {
        return id;
    }

    public WebhookEndpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(WebhookEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    
}

