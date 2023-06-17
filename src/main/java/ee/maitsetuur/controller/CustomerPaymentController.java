package ee.maitsetuur.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.zxing.WriterException;
import ee.maitsetuur.exception.PaymentException;
import ee.maitsetuur.exception.PaymentNotFoundException;
import ee.maitsetuur.request.CertificateVerificationRequest;
import ee.maitsetuur.request.PaymentValidationRequest;
import ee.maitsetuur.request.payment.PaymentRequest;
import ee.maitsetuur.response.CertificateCreationResponse;
import ee.maitsetuur.response.CertificateVerificationResponse;
import ee.maitsetuur.response.PaymentValidationResponse;
import ee.maitsetuur.service.CustomerPaymentService;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("${api.basepath}/payment")
@RequiredArgsConstructor
public class CustomerPaymentController {

    private final CustomerPaymentService service;

    @PostMapping("/initiateCreation")
    public ResponseEntity<CertificateCreationResponse> initiateCreation(
            @Valid @RequestBody PaymentRequest request
    ) throws JsonProcessingException {
        return ResponseEntity.ok(service.initiateCreation(request));
    }

    @PostMapping("/verificationCreation")
    public synchronized ResponseEntity<CertificateVerificationResponse> verificationCreation(
            @Valid @RequestBody CertificateVerificationRequest request
    ) throws PaymentNotFoundException, PaymentException, MessagingException {
        return ResponseEntity.ok(service.verificationCreation(request));
    }

    @PostMapping("/validatePayment")
    public ResponseEntity<PaymentValidationResponse> validatePayment(
            @Valid @RequestBody PaymentValidationRequest request
    ) {
        return ResponseEntity.ok(service.validatePayment(request));
    }
}
