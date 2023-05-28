package ee.maitsetuur.repository;

import ee.maitsetuur.model.business.PaymentCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCustomerRepository extends JpaRepository<PaymentCustomer, Long> {

}
