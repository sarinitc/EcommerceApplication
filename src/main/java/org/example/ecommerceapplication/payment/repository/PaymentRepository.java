package org.example.ecommerceapplication.payment.repository;

import org.example.ecommerceapplication.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
