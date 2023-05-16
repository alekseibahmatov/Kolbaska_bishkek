package ee.maitsetuur.repository;

import ee.maitsetuur.model.certificate.Certificate;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CertificateRepository extends JpaRepository<Certificate, String> {
    @NotNull
    Optional<Certificate> findById(@NotNull String id);
}
