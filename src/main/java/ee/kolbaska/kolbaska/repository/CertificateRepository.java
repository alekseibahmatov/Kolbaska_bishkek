package ee.kolbaska.kolbaska.repository;

import ee.kolbaska.kolbaska.model.certificate.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, String> {
}
