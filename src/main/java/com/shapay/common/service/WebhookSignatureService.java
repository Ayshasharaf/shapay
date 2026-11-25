package com.shapay.common.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

@Service
public class WebhookSignatureService {

    public String generateSignature(String payload, String secret) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );

            mac.init(secretKey);

            byte[] hash = mac.doFinal(
                payload.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                "Could not generate webhook signature", e
            );
        }
    }
    public boolean verifySignature(
    String payload,
    String secret,
    String receivedSignature
    ) {
        String expectedSignature = generateSignature(payload, secret);

        return MessageDigest.isEqual(
            expectedSignature.getBytes(StandardCharsets.UTF_8),
            receivedSignature.getBytes(StandardCharsets.UTF_8)
        );
    }
}