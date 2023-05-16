package ee.maitsetuur.controller;

import ee.maitsetuur.response.ActivationCodeValidationResponse;
import ee.maitsetuur.response.AuthenticationResponse;
import ee.maitsetuur.response.PersonalDataResponse;
import ee.maitsetuur.response.RecoveryResponse;
import ee.maitsetuur.service.AuthenticationService;
import ee.maitsetuur.request.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.basepath}/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
            ) throws Exception {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/recovery/start")
    public ResponseEntity<RecoveryResponse> startRecovery(
            @Valid @RequestBody StartRecoveryRequest request
    ) {
        return ResponseEntity.ok(service.startRecovery(request));
    }

    @PostMapping("/recovery")
    public ResponseEntity<RecoveryResponse> recovery(
            @Valid @RequestBody RecoveryRequest request
            ) {
        return ResponseEntity.ok(service.recovery(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody UserAuthenticationRequest request
            ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/personalData")
    public ResponseEntity<PersonalDataResponse> savePersonalData(
            @Valid @RequestBody PersonalDataRequest request
    ) {
        return ResponseEntity.ok(service.savePersonalData(request));
    }

    @GetMapping("/activationCode/{id}")
    public ResponseEntity<ActivationCodeValidationResponse> validateActivationCode(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(service.validateActivationCode(id));
    }
}
