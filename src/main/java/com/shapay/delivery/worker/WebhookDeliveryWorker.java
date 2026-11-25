
package com.shapay.delivery.worker;

import java.time.LocalDateTime;
import java.util.List;
import com.shapay.delivery.service.WebhookDeliveryClaimService;
import com.shapay.common.service.WebhookSignatureService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.shapay.delivery.entity.DeliveryAttempt;
import com.shapay.delivery.entity.WebhookDelivery;
import com.shapay.delivery.repository.DeliveryAttemptRepository;
import com.shapay.delivery.repository.WebhookDeliveryRepository;

import com.shapay.delivery.service.DeliveryResultService;

@Component
public class WebhookDeliveryWorker {
    private final WebhookDeliveryClaimService claimService;
    private final WebhookSignatureService signatureService;
    private final DeliveryResultService resultService;

    private final WebhookDeliveryRepository deliveryRepository;
    private final RestClient restClient;
    private final DeliveryAttemptRepository attemptRepository;

    public WebhookDeliveryWorker(
        WebhookDeliveryRepository deliveryRepository,
        DeliveryAttemptRepository attemptRepository,
        WebhookDeliveryClaimService claimService,
        WebhookSignatureService signatureService,
         DeliveryResultService resultService


    ) {

        this.deliveryRepository = deliveryRepository;
        this.attemptRepository = attemptRepository;
        this.signatureService = signatureService;
        this.resultService = resultService;

        this.claimService = claimService;

        this.restClient = RestClient.create();
    }

    @Scheduled(fixedDelay = 5000)
    public void processPendingDeliveries() {

        System.out.println("WORKER IS RUNNING");

        List<WebhookDelivery> pendingDeliveries =
             claimService.claimDeliveries(10);

        for (WebhookDelivery delivery : pendingDeliveries) {

            delivery.incrementAttemptCount();

            DeliveryAttempt attempt = new DeliveryAttempt();
            attempt.setDelivery(delivery);
            attempt.setAttemptNumber(delivery.getAttemptCount());

            long startTime = System.currentTimeMillis();

            try {

                String payload = delivery.getWebhookEvent().getPayload();

                String signature = signatureService.generateSignature(
                    payload,
                    delivery.getEndpoint().getSecretKey()
                );

                var response = restClient.post()
                    .uri(delivery.getEndpoint().getUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-ShaPay-Signature", signature)
                    .body(payload)
                    .retrieve()
                    .toEntity(String.class);

                attempt.setStatus("SUCCESS");
                attempt.setResponseCode(response.getStatusCode().value());
                attempt.setResponseBody(response.getBody());

                delivery.setStatus("SUCCESS");
                delivery.setNextAttemptAt(null);

            } catch (Exception e) {

                attempt.setStatus("FAILED");

                if (e instanceof RestClientResponseException responseException) {
                    attempt.setResponseCode(
                        responseException.getStatusCode().value()
                    );
                    attempt.setResponseBody(
                        responseException.getResponseBodyAsString()
                    );
                }

                if (delivery.getAttemptCount() >= 5) {

                    delivery.setStatus("FAILED");
                    delivery.setNextAttemptAt(null);

                    System.out.println(
                        "Maximum attempts reached: " + delivery.getId()
                    );

                } else {

                    long delay = (long) Math.pow(
                        2, delivery.getAttemptCount() - 1
                    ) * 5;

                    delivery.scheduleRetry(delay);

                    System.out.println(
                        "Delivery failed: " + delivery.getId()
                        + " | Next retry in " + delay + " seconds"
                    );
                }

            } finally {

                long duration = System.currentTimeMillis() - startTime;

                attempt.setDurationMs(duration);

                resultService.saveResult(attempt, delivery);
            }
        }
    }
}
