package com.shapay.payment.service;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.shapay.payment.entity.Payment;
import com.shapay.payment.repository.PaymentRepository;

import jakarta.transaction.Transactional;

@Service 
public class PaymentService {
    private final PaymentRepository paymentRepository;
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment createPayment(BigDecimal amount, String currency) {
        Payment payment = new Payment(amount, currency, "SUCCEEDED");   
        return paymentRepository.save(payment);
    }


    
    
}
