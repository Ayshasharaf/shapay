package com.shapay.delivery.repository;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shapay.delivery.entity.WebhookDelivery;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {

    List<WebhookDelivery> findByStatus(String string);
    @Query("""
        SELECT d
        FROM WebhookDelivery d
        JOIN FETCH d.endpoint
        JOIN FETCH d.webhookEvent
        WHERE d.status = :status
    """)
    List<WebhookDelivery> findByStatusWithDetails(@Param("status") String status);
    @Query("""
    SELECT d
    FROM WebhookDelivery d
    JOIN FETCH d.endpoint
    JOIN FETCH d.webhookEvent
    WHERE d.status = 'PENDING'
       OR (d.status = 'RETRY' AND d.nextAttemptAt <= :now)
    """)
    List<WebhookDelivery> findReadyDeliveries(
        @Param("now") LocalDateTime now
    );
    
    @Query(value = """
        WITH ready_deliveries AS (
            SELECT id
            FROM webhook_deliveries
            WHERE status = 'PENDING'
            OR (
                    status = 'RETRY'
                    AND next_attempt_at <= :now
            )
            ORDER BY id
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
        )
        UPDATE webhook_deliveries
        SET status = 'PROCESSING',
            claimed_at = :now
        WHERE id IN (SELECT id FROM ready_deliveries)
        RETURNING id
        """, nativeQuery = true)
    List<Long> claimReadyDeliveryIds(
        @Param("now") LocalDateTime now,
        @Param("batchSize") int batchSize
    );

    @Query("""
        SELECT d
        FROM WebhookDelivery d
        JOIN FETCH d.endpoint
        JOIN FETCH d.webhookEvent
        WHERE d.id IN :ids
        """)
    List<WebhookDelivery> findAllByIdWithDetails(
        @Param("ids") List<Long> ids
    );
    

    // Repository methods for WebhookDelivery entity
    
}
