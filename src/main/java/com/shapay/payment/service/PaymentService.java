package com.shapay.payment.service;
import java.math.BigDecimal;
import java.util.Map;

import org.springframework.boot.json.JsonParseException;
import org.springframework.stereotype.Service;

import com.shapay.payment.entity.Payment;
import com.shapay.payment.repository.PaymentRepository;
import com.shapay.webhook.entity.WebhookEvent;
import com.shapay.webhook.repository.WebhookEventRepository;

import jakarta.transaction.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service 
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final WebhookEventRepository webhookRepository;
    private final ObjectMapper objectMapper;

    public PaymentService(PaymentRepository paymentRepository, WebhookEventRepository webhookRepository, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.webhookRepository = webhookRepository;
        this.objectMapper = objectMapper;
    }


    @Transactional
    public Payment createPayment(BigDecimal amount, String currency) {
        Payment payment = new Payment(amount, currency, "SUCCEEDED");   
        Payment savedPayment = paymentRepository.save(payment);

        Map<String, Object> payload = Map.of(
            "payment_id", savedPayment.getId(),
            "amount", savedPayment.getAmount(),
            "currency", savedPayment.getCurrency(),
            "status", savedPayment.getStatus()
        );
        try {
            // 4. Convert payload to JSON
            String jsonPayload = objectMapper.writeValueAsString(payload);

            // 5. Create webhook event
            WebhookEvent event = new WebhookEvent();
            event.setPayment(savedPayment);
            event.setEventType("payment.succeeded");
            event.setPayload(jsonPayload);

            // 6. Save webhook event
            webhookRepository.save(event);

        } catch (JsonParseException e) {
            throw new RuntimeException("Failed to create webhook payload", e);
        }

        return savedPayment;
    }


    
    
}
