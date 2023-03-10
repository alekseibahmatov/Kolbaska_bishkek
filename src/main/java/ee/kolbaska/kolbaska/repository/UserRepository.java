package ee.kolbaska.kolbaska.repository;

import ee.kolbaska.kolbaska.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByActivationCode(String code);

    Optional<User> findByPersonalCode(String code);

    @Query(nativeQuery = true, value = "SELECT u.* FROM user u INNER JOIN restaurant r ON u.restaurant_id = r.id WHERE u.id NOT IN (SELECT manager_id FROM restaurant)")
    List<User> findAllWaiters();
}
