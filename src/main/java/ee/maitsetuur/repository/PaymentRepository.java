package ee.maitsetuur.repository;

import ee.maitsetuur.model.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByMerchantReference(String reference);
}
