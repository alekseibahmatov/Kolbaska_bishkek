package ee.maitsetuur.repository;

import ee.maitsetuur.model.certificate.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
}
