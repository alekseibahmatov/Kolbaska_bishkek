package ee.kolbaska.kolbaska.repository;

import ee.kolbaska.kolbaska.model.address.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}