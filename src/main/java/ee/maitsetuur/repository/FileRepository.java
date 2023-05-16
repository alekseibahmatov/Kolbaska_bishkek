package ee.maitsetuur.repository;

import ee.maitsetuur.model.file.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findById(UUID id);
}
