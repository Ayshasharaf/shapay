package com.shapay.delivery.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.shapay.delivery.entity.WebhookDelivery;
import com.shapay.delivery.repository.WebhookDeliveryRepository;

@Service
public class WebhookReplayService {

    private final WebhookDeliveryRepository deliveryRepository;

    public WebhookReplayService(
        WebhookDeliveryRepository deliveryRepository
    ) {
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional
    public WebhookDelivery replay(Long deliveryId) {

        WebhookDelivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Delivery not found"
            ));

        if (!delivery.getStatus().equals("FAILED")) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Only failed deliveries can be replayed"
            );
        }

        delivery.resetForReplay();

        return deliveryRepository.save(delivery);
    }
}