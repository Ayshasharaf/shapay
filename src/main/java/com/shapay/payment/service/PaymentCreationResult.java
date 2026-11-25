package com.shapay.payment.service;

import com.shapay.payment.entity.Payment;

public record PaymentCreationResult(
    Payment payment,
    boolean created
) {}