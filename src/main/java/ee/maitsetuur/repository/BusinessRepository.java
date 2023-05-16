package ee.maitsetuur.repository;

import ee.maitsetuur.model.business.Business;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessRepository extends JpaRepository<Business, Long> {
}
