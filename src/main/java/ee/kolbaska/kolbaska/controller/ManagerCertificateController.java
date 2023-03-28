package ee.kolbaska.kolbaska.controller;

import com.google.zxing.WriterException;
import ee.kolbaska.kolbaska.exception.CertificateInsufficientFundsException;
import ee.kolbaska.kolbaska.exception.CertificateIsDisabledException;
import ee.kolbaska.kolbaska.exception.CertificateIsOutDatedException;
import ee.kolbaska.kolbaska.exception.CertificateNotFoundException;
import ee.kolbaska.kolbaska.request.CertificateActivationRequest;
import ee.kolbaska.kolbaska.response.CertificateActivationResponse;
import ee.kolbaska.kolbaska.service.ManagerCertificateService;
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
@RequestMapping("${api.basepath}/transaction")
@RequiredArgsConstructor
public class ManagerCertificateController {

    private final ManagerCertificateService service;

    @PostMapping("/certificate")
    public ResponseEntity<CertificateActivationResponse> activateCertificate(
            @Valid @RequestBody CertificateActivationRequest request
    ) throws IOException, CertificateNotFoundException, CertificateInsufficientFundsException, CertificateIsDisabledException, CertificateIsOutDatedException, MessagingException, TemplateException, WriterException {
        return ResponseEntity.ok(service.activateCertificate(request));
    }
}
