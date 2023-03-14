package ee.kolbaska.kolbaska.controller;

import ee.kolbaska.kolbaska.exception.CertificateInsufficientFundsException;
import ee.kolbaska.kolbaska.exception.CertificateIsDisabledException;
import ee.kolbaska.kolbaska.exception.CertificateIsOutDatedException;
import ee.kolbaska.kolbaska.exception.CertificateNotFoundException;
import ee.kolbaska.kolbaska.request.CertificateActivationRequest;
import ee.kolbaska.kolbaska.response.CertificateActivationResponse;
import ee.kolbaska.kolbaska.service.ManagerTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("${api.basepath}/transaction")
@RequiredArgsConstructor
public class ManagerTransactionController {

    private final ManagerTransactionService service;

    @PostMapping("/certificate")
    public ResponseEntity<CertificateActivationResponse> activateCertificate(
            @Valid @RequestBody CertificateActivationRequest request
    ) throws AccessDeniedException, CertificateNotFoundException, CertificateInsufficientFundsException, CertificateIsDisabledException, CertificateIsOutDatedException {
        return ResponseEntity.ok(service.activateCertificate(request));
    }
}
