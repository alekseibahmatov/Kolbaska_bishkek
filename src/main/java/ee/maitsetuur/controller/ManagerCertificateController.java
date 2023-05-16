package ee.maitsetuur.controller;

import com.google.zxing.WriterException;
import ee.maitsetuur.exception.CertificateInsufficientFundsException;
import ee.maitsetuur.exception.CertificateIsDisabledException;
import ee.maitsetuur.exception.CertificateIsOutDatedException;
import ee.maitsetuur.exception.CertificateNotFoundException;
import ee.maitsetuur.request.CertificateActivationRequest;
import ee.maitsetuur.response.CertificateActivationResponse;
import ee.maitsetuur.service.ManagerCertificateService;
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
