package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.model.certificate.Certificate;
import ee.kolbaska.kolbaska.repository.CertificateRepository;
import ee.kolbaska.kolbaska.request.CertificateCreationRequest;
import ee.kolbaska.kolbaska.response.CertificateCreationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final CertificateRepository certificateRepository;

    public CertificateCreationResponse initiateCreation(CertificateCreationRequest request) {
        Certificate certificate = Certificate.builder()
                .active(false)
                .activationCode(UUID.randomUUID().toString())
                .description(request.getCongratsText())
                .value(request.getValue())
                .build();

        certificateRepository.save(certificate);

        //TODO here add implementation of montonio payment system

        return CertificateCreationResponse.builder()
                .redirectUrl("https://dummymontonio.com/someshittyoreder")
                .build();
    }
}
