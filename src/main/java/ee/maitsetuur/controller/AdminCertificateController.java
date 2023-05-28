package ee.maitsetuur.controller;

import com.google.zxing.WriterException;
import ee.maitsetuur.exception.CertificateNotFoundException;
import ee.maitsetuur.request.AdminCertificateCreationRequest;
import ee.maitsetuur.request.AdminUpdateCertificateInformationRequest;
import ee.maitsetuur.response.AdminCertificateCreationResponse;
import ee.maitsetuur.response.AdminCertificateInformationResponse;
import ee.maitsetuur.response.AdminCertificateResponse;
import ee.maitsetuur.response.AdminUpdateCertificateInformationResponse;
import ee.maitsetuur.service.AdminCertificateService;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.basepath}/admin")
@RequiredArgsConstructor
public class AdminCertificateController {

    private final AdminCertificateService service;

    @PostMapping("/certificate")
    public ResponseEntity<AdminCertificateCreationResponse> createCertificate(
            @Valid @RequestBody AdminCertificateCreationRequest request
    ) throws MessagingException, IOException, WriterException {
        return ResponseEntity.ok(service.createCertificate(request));
    }

    @GetMapping("/certificate")
    public ResponseEntity<List<AdminCertificateResponse>> getCertificates() {
        return ResponseEntity.ok(service.getCertificates());
    }

    @GetMapping("/certificate/{id}")
    public ResponseEntity<AdminCertificateInformationResponse> getCertificate(
            @Valid @PathVariable UUID id
    ) throws CertificateNotFoundException {
        return ResponseEntity.ok(service.getCertificate(id));
    }

    @PutMapping("/certificate")
    public ResponseEntity<AdminUpdateCertificateInformationResponse> updateCertificate(
            @Valid @RequestBody AdminUpdateCertificateInformationRequest request
    ) throws CertificateNotFoundException {
        return ResponseEntity.ok(service.updateCertificate(request));
    }

    @DeleteMapping("/certificate/{id}")
    public ResponseEntity<AdminUpdateCertificateInformationResponse> disableCertificate(
            @Valid @PathVariable UUID id
    ) throws CertificateNotFoundException {
        return ResponseEntity.ok(service.disableCertificate(id));
    }
}
