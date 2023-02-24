package ee.kolbaska.kolbaska.repository;

import ee.kolbaska.kolbaska.model.certificate.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CertificateRepository extends JpaRepository<Certificate, String> {
    Optional<Certificate> findById(String id);
}
