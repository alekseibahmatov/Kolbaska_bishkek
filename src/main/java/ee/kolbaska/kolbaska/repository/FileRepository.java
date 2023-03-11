package ee.kolbaska.kolbaska.repository;

import ee.kolbaska.kolbaska.model.file.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findById(String id);
}
