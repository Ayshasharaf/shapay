package com.shapay.delivery.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shapay.delivery.entity.DeliveryAttempt;
import com.shapay.delivery.entity.WebhookDelivery;
import com.shapay.delivery.repository.DeliveryAttemptRepository;
import com.shapay.delivery.repository.WebhookDeliveryRepository;

@Service
public class DeliveryResultService {

    private final DeliveryAttemptRepository attemptRepository;
    private final WebhookDeliveryRepository deliveryRepository;

    public DeliveryResultService(
        DeliveryAttemptRepository attemptRepository,
        WebhookDeliveryRepository deliveryRepository
    ) {
        this.attemptRepository = attemptRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional
    public void saveResult(
        DeliveryAttempt attempt,
        WebhookDelivery delivery
    ) {
        attemptRepository.save(attempt);
        deliveryRepository.save(delivery);
    }
}