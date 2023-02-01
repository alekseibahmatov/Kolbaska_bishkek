package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.model.user.Role;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.RoleRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.AuthenticationRequest;
import ee.kolbaska.kolbaska.request.RecoveryRequest;
import ee.kolbaska.kolbaska.request.RegisterRequest;
import ee.kolbaska.kolbaska.request.StartRecoveryRequest;
import ee.kolbaska.kolbaska.response.AuthenticationResponse;
import ee.kolbaska.kolbaska.response.RecoveryResponse;
import ee.kolbaska.kolbaska.security.JwtService;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;


import javax.management.relation.RoleNotFoundException;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestPropertySource("/tests.properties")

class AuthenticationServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private EmailService emailService;
    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    void setUpRole() {
        Role role = new Role();
        role.setRoleName("ROLE_CUSTOMER");
        when(roleRepository.findRoleByRoleName(anyString())).thenReturn(Optional.of(role));
    }

    @Test
    void testRegister() throws Exception {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(jwtService.createToken(anyMap(), any(User.class))).thenReturn("token");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@test.com");
        registerRequest.setPassword("password");

        AuthenticationResponse response = authenticationService.register(registerRequest);
        assertEquals("token", response.getToken());
    }

    @Test
    void testRegister_RoleNotFound() {
        when(roleRepository.findRoleByRoleName(anyString())).thenReturn(Optional.empty());

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@test.com");
        registerRequest.setPassword("password");

        assertThrows(RoleNotFoundException.class, () -> authenticationService.register(registerRequest));
    }

    @Test
    void testAuthenticate() {
        User user = new User();
        user.setRole(roleRepository.findRoleByRoleName("ROLE_CUSTOMER").get());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtService.createToken(anyMap(), any(User.class))).thenReturn("token");

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail("test@test.com");
        authenticationRequest.setPassword("password");

        AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);
        assertEquals("token", response.getToken());
    }

    @Test
    void testAuthenticate_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail("test@test.com");
        authenticationRequest.setPassword("password");

        assertThrows(NoSuchElementException.class, () -> authenticationService.authenticate(authenticationRequest));
    }

    @Test
    void startRecovery_whenUserWithEmailExists_thenReturnRecoveryResponse() {
        StartRecoveryRequest startRecoveryRequest = new StartRecoveryRequest("test@test.com");
        User user = new User();
        user.setEmail("test@test.com");
        when(userRepository.findByEmail(startRecoveryRequest.getEmail())).thenReturn(Optional.of(user));

        RecoveryResponse recoveryResponse = authenticationService.startRecovery(startRecoveryRequest);

        assertEquals("Recovery link was sent to email", recoveryResponse.getMessage());
        verify(userRepository, times(1)).save(user);
        verify(emailService, times(1)).sendSimpleMessage(eq(user.getEmail()), eq("Password recovery"), any());
    }

    @Test
    void startRecovery_whenUserWithEmailDoesNotExist_thenThrowUsernameNotFoundException() {
        StartRecoveryRequest startRecoveryRequest = new StartRecoveryRequest("test@test.com");
        when(userRepository.findByEmail(startRecoveryRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> authenticationService.startRecovery(startRecoveryRequest));
        verify(emailService, times(0)).sendSimpleMessage(anyString(), anyString(), anyString());
    }

    @Test
    void recovery_whenUserWithActivationCodeExists_thenReturnRecoveryResponse() {
        RecoveryRequest recoveryRequest = new RecoveryRequest("test", "test-code");
        User user = new User();
        user.setActivationCode("test-code");
        when(userRepository.findByActivationCode(recoveryRequest.getActivationCode())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(recoveryRequest.getNewPassword())).thenReturn("encoded-password");

        RecoveryResponse recoveryResponse = authenticationService.recovery(recoveryRequest);

        assertEquals("Password was successfully reset", recoveryResponse.getMessage());
        verify(userRepository, times(1)).save(user);
    }
}

