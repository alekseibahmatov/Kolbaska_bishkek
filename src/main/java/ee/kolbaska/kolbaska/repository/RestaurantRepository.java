package ee.kolbaska.kolbaska.repository;

import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findAll();

    Optional<Restaurant> findByEmail(String email);

    Optional<Restaurant> findByRestaurantCode(String restaurantCode);
}
