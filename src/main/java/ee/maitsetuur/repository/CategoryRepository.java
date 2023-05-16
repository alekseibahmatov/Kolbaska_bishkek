package ee.maitsetuur.repository;

import ee.maitsetuur.model.category.Category;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @NotNull
    List<Category> findAll();

    Optional<Category> findByName(String categoryName);
}
