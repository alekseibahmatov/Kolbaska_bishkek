package ee.kolbaska.kolbaska.controller;

import ee.kolbaska.kolbaska.exception.CertificateInsufficientFundsException;
import ee.kolbaska.kolbaska.exception.CertificateIsDisabledException;
import ee.kolbaska.kolbaska.exception.CertificateIsOutDatedException;
import ee.kolbaska.kolbaska.exception.CertificateNotFoundException;
import ee.kolbaska.kolbaska.request.CertificateActivationRequest;
import ee.kolbaska.kolbaska.response.CertificateActivationResponse;
import ee.kolbaska.kolbaska.service.TransactionService;
import jakarta.validation.constraints.NotNull;
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
public class TransactionController {

    private final TransactionService service;

    @PostMapping("/certificate")
    public ResponseEntity<CertificateActivationResponse> activateCertificate(
            @NotNull @RequestBody CertificateActivationRequest request
    ) throws AccessDeniedException, CertificateNotFoundException, CertificateInsufficientFundsException, CertificateIsDisabledException, CertificateIsOutDatedException {
        return ResponseEntity.ok(service.activateCertificate(request));
    }
}
