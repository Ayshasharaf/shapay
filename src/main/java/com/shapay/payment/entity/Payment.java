package com.shapay.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity 
@Table( name = "payments" )
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @Column (nullable = false)
    private BigDecimal amount;
    @Column (nullable = false, length = 3)
    private String currency;
    @Column (nullable = false, length = 20)
    private String status;
    @Column (nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "request_hash", length = 64)
    private String requestHash;

    public Payment() {
    }
    public Payment(BigDecimal amount, String currency, String status) {
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }
    public long getId() {
        return id;
    }
    public String getIdempotencyKey() {
    return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }
    
    public BigDecimal getAmount() {
        return amount;  
    }    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
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
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}   