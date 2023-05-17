package ee.maitsetuur.repository;

import ee.maitsetuur.model.report.Report;
import ee.maitsetuur.model.restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByCreatedAtAfterAndCreatedAtBeforeAndRestaurant(Instant start, Instant end, Restaurant restaurant);
}
