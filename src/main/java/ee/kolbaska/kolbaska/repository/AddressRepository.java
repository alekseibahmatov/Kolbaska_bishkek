package ee.kolbaska.kolbaska.repository;

import ee.kolbaska.kolbaska.model.address.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByStreetAndCityAndApartmentNumberAndCountryAndStateAndZipCode(String street, String city, String apartmentNumber, String country, String state, String zipCode);
}