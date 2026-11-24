package com.shapay.webhook.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity 
@Table (name = "webhook_endpoints")
public class WebhookEndpoint {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private long id;
    @Column(name = "url", nullable = false, length = 2048)
    private String url;
    @Column(name = "secret_key", nullable = false, length = 255)
    private String secretKey;
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public WebhookEndpoint() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public WebhookEndpoint(String url, String secretKey, String status) {
        this.url = url;
        this.secretKey = secretKey;
        this.status = status;
    }
      public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    
}
