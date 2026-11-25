package com.shapay.delivery.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.shapay.delivery.entity.WebhookDelivery;
import com.shapay.delivery.repository.WebhookDeliveryRepository;
import com.shapay.webhook.entity.WebhookEndpointEvent;
import com.shapay.webhook.entity.WebhookEvent;
import com.shapay.webhook.repository.WebhookEndpointEventRepository;

import jakarta.transaction.Transactional;

@Service 
public class WebhookDeliveryService {

    private final WebhookEndpointEventRepository subscriptionRepository;
    private final WebhookDeliveryRepository deliveryRepository;    
    public WebhookDeliveryService(
        WebhookEndpointEventRepository subscriptionRepository,
        WebhookDeliveryRepository deliveryRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional
    public void createDeliveries(WebhookEvent event) {

        // 1. Find all merchants subscribed to this event
        List<WebhookEndpointEvent> subscriptions =
            subscriptionRepository.findByEventType(event.getEventType());

        // 2. Create one delivery job for each subscribed endpoint
        for (WebhookEndpointEvent subscription : subscriptions) {

            WebhookDelivery delivery = new WebhookDelivery(
                event,
                subscription.getEndpoint()
            );

            // 3. Save the delivery job
            deliveryRepository.save(delivery);
        }
    }

        
}
