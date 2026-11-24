package com.shapay.payment.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shapay.payment.entity.Payment;
import com.shapay.payment.service.PaymentService;

@RestController 
@RequestMapping("/api/payments")
public class PaymentController {
     private final PaymentService paymentService;
     public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping 
    @ResponseStatus(HttpStatus.CREATED)
    public Payment createPayment(@RequestParam BigDecimal amount, @RequestParam String currency) {
        // Logic to create a payment
        return paymentService.createPayment(amount, currency);
        
    }
    
}
