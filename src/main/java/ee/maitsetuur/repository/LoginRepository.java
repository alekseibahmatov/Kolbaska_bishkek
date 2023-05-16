package ee.maitsetuur.repository;

import ee.maitsetuur.model.login.Login;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginRepository extends JpaRepository<Login, Long> {
}
