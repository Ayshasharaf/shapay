package com.shapay.payment.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.shapay.payment.entity.Payment;
import com.shapay.payment.service.PaymentCreationResult;
import com.shapay.payment.service.PaymentService;

@RestController 
@RequestMapping("/api/payments")
public class PaymentController {
     private final PaymentService paymentService;
     public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
public ResponseEntity<Payment> createPayment(
    @RequestParam BigDecimal amount,
    @RequestParam String currency,
    @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        PaymentCreationResult result = paymentService.createPayment(
            amount,
            currency,
            idempotencyKey
        );

        HttpStatus status = result.created()
            ? HttpStatus.CREATED
            : HttpStatus.OK;

        return ResponseEntity
            .status(status)
            .body(result.payment());
    }
    
}
