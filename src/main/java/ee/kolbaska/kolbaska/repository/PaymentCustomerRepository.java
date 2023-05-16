package ee.kolbaska.kolbaska.repository;

import ee.kolbaska.kolbaska.model.business.PaymentCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCustomerRepository extends JpaRepository<PaymentCustomer, Long> {

}
