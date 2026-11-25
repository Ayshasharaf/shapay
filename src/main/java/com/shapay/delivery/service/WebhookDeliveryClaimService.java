package com.shapay.delivery.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shapay.delivery.entity.WebhookDelivery;
import com.shapay.delivery.repository.WebhookDeliveryRepository;

@Service
public class WebhookDeliveryClaimService {

    private final WebhookDeliveryRepository deliveryRepository;

    public WebhookDeliveryClaimService(
        WebhookDeliveryRepository deliveryRepository
    ) {
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional
    public List<WebhookDelivery> claimDeliveries(int batchSize) {

        LocalDateTime now = LocalDateTime.now();

        List<Long> claimedIds =
            deliveryRepository.claimReadyDeliveryIds(now, batchSize);

        if (claimedIds.isEmpty()) {
            return List.of();
        }

        return deliveryRepository.findAllByIdWithDetails(claimedIds);
    }
}