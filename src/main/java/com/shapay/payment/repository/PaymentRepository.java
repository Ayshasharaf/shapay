
package com.shapay.payment.repository;
import com.shapay.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PaymentRepository extends JpaRepository<Payment, Long>  {
    // Repository methods for Payment entity
}





