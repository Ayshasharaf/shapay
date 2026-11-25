package com.shapay.payment.service;
import java.math.BigDecimal;
import java.util.Map;
import tools.jackson.core.JacksonException;

import org.springframework.stereotype.Service;
import com.shapay.common.service.RequestHashService;

import com.shapay.payment.entity.Payment;
import com.shapay.payment.repository.PaymentRepository;
import com.shapay.webhook.entity.WebhookEvent;
import com.shapay.webhook.repository.WebhookEventRepository;

import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import com.shapay.delivery.service.WebhookDeliveryService;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service 
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final WebhookEventRepository webhookRepository;
    private final ObjectMapper objectMapper;
    private final WebhookDeliveryService webhookDeliveryService;
    private final RequestHashService requestHashService;

    public PaymentService(PaymentRepository paymentRepository, WebhookEventRepository webhookRepository, ObjectMapper objectMapper, WebhookDeliveryService webhookDeliveryService, RequestHashService requestHashService) {
        this.paymentRepository = paymentRepository;
        this.webhookRepository = webhookRepository;
        this.objectMapper = objectMapper;
        this.webhookDeliveryService = webhookDeliveryService;
        this.requestHashService = requestHashService;
    }


    @Transactional
    public PaymentCreationResult createPayment(
    BigDecimal amount,
    String currency,
    String idempotencyKey) {
        String requestHash = requestHashService.generateHash(amount, currency);

        int inserted = paymentRepository.insertIfAbsent(
            amount,
            currency,
            idempotencyKey,
            requestHash
        );

        if (inserted == 0) {
            Payment existing = paymentRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElseThrow();

            if (!existing.getRequestHash().equals(requestHash)) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Idempotency key already used with different payment details"
                );
            }

            return new PaymentCreationResult(existing, false);
        }

        Payment savedPayment = paymentRepository
            .findByIdempotencyKey(idempotencyKey)
            .orElseThrow();

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
            WebhookEvent savedEvent = webhookRepository.save(event);    
            webhookDeliveryService.createDeliveries(savedEvent);

        } catch (JacksonException e) {
            throw new RuntimeException("Failed to create webhook payload", e);
        }

        return new PaymentCreationResult(savedPayment, true);
    }


    
    
}
