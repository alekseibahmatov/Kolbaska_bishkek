package ee.maitsetuur.repository;

import ee.maitsetuur.model.payment.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByMerchantReference(String reference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.merchantReference = :merchantReference")
    Optional<Payment> findByMerchantReferenceWithLock(@Param("merchantReference") String merchantReference);

}
