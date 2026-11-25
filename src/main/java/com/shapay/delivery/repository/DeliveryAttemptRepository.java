package com.shapay.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shapay.delivery.entity.DeliveryAttempt;

public interface DeliveryAttemptRepository
        extends JpaRepository<DeliveryAttempt, Long> {

}
