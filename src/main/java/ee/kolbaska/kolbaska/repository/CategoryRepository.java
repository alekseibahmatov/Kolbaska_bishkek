package ee.kolbaska.kolbaska.repository;

import ee.kolbaska.kolbaska.model.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAll();

    Optional<Category> findByName(String categoryName);
}
