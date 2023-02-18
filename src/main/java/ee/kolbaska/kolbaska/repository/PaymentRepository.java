package ee.kolbaska.kolbaska.repository;

import ee.kolbaska.kolbaska.model.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findById(String id);
}
