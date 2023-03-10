package ee.kolbaska.kolbaska.controller;

import com.google.zxing.WriterException;
import ee.kolbaska.kolbaska.exception.CertificateNotFoundException;
import ee.kolbaska.kolbaska.request.AdminCertificateCreationRequest;
import ee.kolbaska.kolbaska.request.AdminUpdateCertificateInformationRequest;
import ee.kolbaska.kolbaska.response.AdminCertificateCreationResponse;
import ee.kolbaska.kolbaska.response.AdminCertificateInformationResponse;
import ee.kolbaska.kolbaska.response.AdminCertificateResponse;
import ee.kolbaska.kolbaska.response.AdminUpdateCertificateInformationResponse;
import ee.kolbaska.kolbaska.service.AdminCertificateService;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("${api.basepath}/admin")
@RequiredArgsConstructor
public class AdminCertificateController {

    private final AdminCertificateService service;

    @PostMapping("/certificate")
    public ResponseEntity<AdminCertificateCreationResponse> createCertificate(
            @NotNull @RequestBody AdminCertificateCreationRequest request
    ) throws MessagingException, TemplateException, IOException, WriterException {
        return ResponseEntity.ok(service.createCertificate(request));
    }

    @GetMapping("/certificate")
    public ResponseEntity<List<AdminCertificateResponse>> getCertificates() {
        return ResponseEntity.ok(service.getCertificates());
    }

    @GetMapping("/certificate/{id}")
    public ResponseEntity<AdminCertificateInformationResponse> getCertificate(
            @NotNull @PathVariable String id
    ) throws CertificateNotFoundException {
        return ResponseEntity.ok(service.getCertificate(id));
    }

    @PutMapping("/certificate")
    public ResponseEntity<AdminUpdateCertificateInformationResponse> updateCertificate(
            @NotNull @RequestBody AdminUpdateCertificateInformationRequest request
    ) throws CertificateNotFoundException {
        return ResponseEntity.ok(service.updateCertificate(request));
    }

    @DeleteMapping("/certificate/{id}")
    public ResponseEntity<AdminUpdateCertificateInformationResponse> disableCertificate(
            @NotNull @PathVariable String id
    ) throws CertificateNotFoundException {
        return ResponseEntity.ok(service.disableCertificate(id));
    }
}
