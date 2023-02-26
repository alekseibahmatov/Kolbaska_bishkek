package ee.kolbaska.kolbaska.controller;

import ee.kolbaska.kolbaska.request.*;
import ee.kolbaska.kolbaska.response.AuthenticationResponse;
import ee.kolbaska.kolbaska.response.PersonalDataResponse;
import ee.kolbaska.kolbaska.response.RecoveryResponse;
import ee.kolbaska.kolbaska.service.AuthenticationService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.basepath}/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @NotNull @RequestBody RegisterRequest request
            ) throws Exception {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/recovery/start")
    public ResponseEntity<RecoveryResponse> startRecovery(
            @NotNull @RequestBody StartRecoveryRequest request
    ) {
        return ResponseEntity.ok(service.startRecovery(request));
    }

    @PostMapping("/recovery")
    public ResponseEntity<RecoveryResponse> recovery(
            @NotNull @RequestBody RecoveryRequest request
            ) {
        return ResponseEntity.ok(service.recovery(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @NotNull @RequestBody UserAuthenticationRequest request
            ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/personalData")
    public ResponseEntity<PersonalDataResponse> savePersonalData(
            @NotNull @RequestBody PersonalDataRequest request
    ) {
        return ResponseEntity.ok(service.savePersonalData(request));
    }
}
