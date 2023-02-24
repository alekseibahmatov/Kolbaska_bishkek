package ee.kolbaska.kolbaska.controller;

import com.google.zxing.WriterException;
import ee.kolbaska.kolbaska.exception.PaymentException;
import ee.kolbaska.kolbaska.exception.PaymentNotFoundException;
import ee.kolbaska.kolbaska.request.CertificateCreationRequest;
import ee.kolbaska.kolbaska.request.CertificateVerificationRequest;
import ee.kolbaska.kolbaska.response.CertificateCreationResponse;
import ee.kolbaska.kolbaska.response.CertificateVerificationResponse;
import ee.kolbaska.kolbaska.service.PaymentService;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.validation.constraints.NotNull;
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
public class PaymentController {

    private final PaymentService service;

    @PostMapping("/initiateCreation")
    public ResponseEntity<CertificateCreationResponse> initiateCreation(
            @NotNull @RequestBody CertificateCreationRequest request
    ) {
        return ResponseEntity.ok(service.initiateCreation(request));
    }

    @PostMapping("/verificationCreation")
    public ResponseEntity<CertificateVerificationResponse> verificationCreation(
            @NotNull @RequestBody CertificateVerificationRequest request
    ) throws PaymentNotFoundException, PaymentException, MessagingException, IOException, WriterException, TemplateException {
        return ResponseEntity.ok(service.verificationCreation(request));
    }
}
