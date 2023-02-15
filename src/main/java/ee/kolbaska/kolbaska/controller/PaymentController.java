package ee.kolbaska.kolbaska.controller;

import ee.kolbaska.kolbaska.request.CertificateCreationRequest;
import ee.kolbaska.kolbaska.response.CertificateCreationResponse;
import ee.kolbaska.kolbaska.service.PaymentService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
