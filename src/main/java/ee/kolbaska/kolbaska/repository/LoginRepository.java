package ee.kolbaska.kolbaska.repository;

import ee.kolbaska.kolbaska.model.login.Login;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginRepository extends JpaRepository<Login, Long> {
}
