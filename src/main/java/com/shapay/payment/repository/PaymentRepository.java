
package com.shapay.payment.repository;
import com.shapay.payment.entity.Payment;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface PaymentRepository extends JpaRepository<Payment, Long>  {
    // Repository methods for Payment entity
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    @Modifying
    @Query(value = """
        INSERT INTO payments (
            amount,
            currency,
            status,
            idempotency_key,
            request_hash
        )
        VALUES (
            :amount,
            :currency,
            'SUCCEEDED',
            :idempotencyKey,
            :requestHash
        )
        ON CONFLICT (idempotency_key) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(
        @Param("amount") BigDecimal amount,
        @Param("currency") String currency,
        @Param("idempotencyKey") String idempotencyKey,
        @Param("requestHash") String requestHash
    );
}





