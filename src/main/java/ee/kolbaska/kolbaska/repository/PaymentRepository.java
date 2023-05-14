package ee.kolbaska.kolbaska.repository;

import ee.kolbaska.kolbaska.model.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findById(UUID id);
}
