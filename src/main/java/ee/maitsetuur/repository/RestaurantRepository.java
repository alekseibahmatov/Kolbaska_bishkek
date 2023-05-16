package ee.maitsetuur.repository;

import ee.maitsetuur.model.restaurant.Restaurant;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    @NotNull
    List<Restaurant> findAll();

    Optional<Restaurant> findByEmail(String email);

    Optional<Restaurant> findByRestaurantCode(String restaurantCode);
}
