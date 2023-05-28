package ee.maitsetuur.service;

import ee.maitsetuur.exception.UserAlreadyExistsException;
import ee.maitsetuur.model.address.Address;
import ee.maitsetuur.model.user.Role;
import ee.maitsetuur.model.user.User;
import ee.maitsetuur.repository.AddressRepository;
import ee.maitsetuur.repository.RoleRepository;
import ee.maitsetuur.repository.UserRepository;
import ee.maitsetuur.request.*;
import ee.maitsetuur.response.ActivationCodeValidationResponse;
import ee.maitsetuur.response.AuthenticationResponse;
import ee.maitsetuur.response.PersonalDataResponse;
import ee.maitsetuur.response.RecoveryResponse;
import ee.maitsetuur.security.JwtService;
import ee.maitsetuur.service.miscellaneous.EmailService;
import ee.maitsetuur.service.miscellaneous.FormatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private FormatService formatService;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterNewUser() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", "password123");

        Role role = Role.builder()
                .roleName("ROLE_CUSTOMER")
                .build();

        User newUser = User.builder()
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword())
                .roles(List.of(role))
                .build();

        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findRoleByRoleName("ROLE_CUSTOMER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtService.createToken(any(), any(User.class))).thenReturn("jwt_token");

        // Act
        AuthenticationResponse authenticationResponse = authenticationService.register(registerRequest);

        // Assert
        assertNotNull(authenticationResponse.getToken());
        verify(userRepository).findByEmail(registerRequest.getEmail());
        verify(roleRepository).findRoleByRoleName("ROLE_CUSTOMER");
        verify(userRepository).save(any(User.class));
        verify(jwtService).createToken(any(), any(User.class));
    }

    @Test
    void testRegisterExistingUser() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", "password123");
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(registerRequest));
        verify(userRepository).findByEmail(registerRequest.getEmail());
        verifyNoMoreInteractions(roleRepository, userRepository, jwtService);
    }

    @Test
    void testAuthenticateUser() {
        // Arrange
        UserAuthenticationRequest userAuthenticationRequest = new UserAuthenticationRequest("test@example.com", "password123");

        Role customerRole = Role.builder()
                .roleName("ROLE_CUSTOMER")
                .build();

        User user = User.builder()
                .email(userAuthenticationRequest.getEmail())
                .password(userAuthenticationRequest.getPassword())
                .roles(List.of(customerRole))
                .build();

        when(userRepository.findByEmail(userAuthenticationRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.createToken(any(), any(User.class))).thenReturn("jwt_token");

        // Act
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(userAuthenticationRequest);

        // Assert
        assertNotNull(authenticationResponse.getToken());
        verify(userRepository).findByEmail(userAuthenticationRequest.getEmail());
        verify(jwtService).createToken(any(), any(User.class));
    }

    @Test
    void testAuthenticateInvalidUser() {
        // Arrange
        UserAuthenticationRequest userAuthenticationRequest = new UserAuthenticationRequest("test@example.com", "password123");
        when(userRepository.findByEmail(userAuthenticationRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.authenticate(userAuthenticationRequest));
        verify(userRepository).findByEmail(userAuthenticationRequest.getEmail());
        verifyNoMoreInteractions(jwtService);
    }

    @Test
    void testStartRecovery() {
        // Arrange
        StartRecoveryRequest startRecoveryRequest = new StartRecoveryRequest("test@example.com");

        User user = User.builder()
                .email(startRecoveryRequest.getEmail())
                .build();

        when(userRepository.findByEmail(startRecoveryRequest.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(emailService).sendSimpleMessage(anyString(), anyString(), anyString());
        // Act
        RecoveryResponse recoveryResponse = authenticationService.startRecovery(startRecoveryRequest);

        // Assert
        assertEquals("Recovery link was sent to email", recoveryResponse.getMessage());

        assertNotNull(user.getActivationCode());
        verify(userRepository).findByEmail(startRecoveryRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(emailService).sendSimpleMessage(anyString(), anyString(), anyString());
    }

    @Test
    void testRecovery() {
        // Arrange
        String activationCode = "activation_code";

        RecoveryRequest recoveryRequest = new RecoveryRequest(activationCode, "new_password");

        User user = User.builder()
                .activationCode(activationCode)
                .build();

        when(userRepository.findByActivationCode(activationCode)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");

        // Act
        RecoveryResponse recoveryResponse = authenticationService.recovery(recoveryRequest);

        // Assert
        assertEquals("Password was successfully reset", recoveryResponse.getMessage());
        assertNotNull(user.getPassword());
        verify(userRepository).findByActivationCode(activationCode);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testSavePersonalData() {
        // Arrange
        PersonalDataRequest personalDataRequest = new PersonalDataRequest("activation_code", "Vanjok Tutov", "+37257575654",
                "12345678901", new AddressRequest("123 Main St", "2", "Anytown", "CA", "12345", "USA"), "test_password");

        Role customerRole = Role.builder()
                .roleName("ROLE_CUSTOMER")
                .build();

        User user = User.builder()
                .activationCode(personalDataRequest.getActivationCode())
                .roles(List.of(customerRole))
                .build();

        when(userRepository.findByActivationCode(personalDataRequest.getActivationCode())).thenReturn(Optional.of(user));
        when(addressRepository.save(any(Address.class))).thenReturn(new Address());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(formatService.formatE164(personalDataRequest.getPhone())).thenReturn("+11234567890");
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");

        // Act
        PersonalDataResponse personalDataResponse = authenticationService.savePersonalData(personalDataRequest);

        // Assert
        assertEquals("Personal data was successfully saved!", personalDataResponse.getMessage());
        assertNotNull(user.getFullName());
        assertNotNull(user.getPassword());
        assertNotNull(user.getAddress());
        assertNotNull(user.getPhone());
        assertTrue(user.getActivated());

        verify(userRepository).findByActivationCode(personalDataRequest.getActivationCode());
        verify(addressRepository).save(any(Address.class));
        verify(userRepository).save(any(User.class));
        verify(formatService).formatE164(personalDataRequest.getPhone());
    }

    @Test
    void testSavePersonalDataWithInvalidActivationCode() {
        // Arrange
        PersonalDataRequest personalDataRequest = new PersonalDataRequest("activation_code", "Vanjok Tutov", "+37257575654",
                "12345678901", new AddressRequest("123 Main St", "2", "Anytown", "CA", "12345", "USA"), "test_password");

        when(userRepository.findByActivationCode(personalDataRequest.getActivationCode())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.savePersonalData(personalDataRequest));
        verify(userRepository).findByActivationCode(personalDataRequest.getActivationCode());
        verifyNoMoreInteractions(addressRepository, userRepository, formatService);
    }

    @Test
    void testValidateActivationCode() {
        // Arrange
        String activationCode = "activation_code";
        when(userRepository.findByActivationCode(activationCode)).thenReturn(Optional.of(new User()));

        // Act
        ActivationCodeValidationResponse response = authenticationService.validateActivationCode(activationCode);

        // Assert
        assertEquals("Activation code valid", response.getMessage());
        verify(userRepository).findByActivationCode(activationCode);
    }

    @Test
    void testValidateInvalidActivationCode() {
        // Arrange
        String invalidActivationCode = "invalid_activation_code";
        when(userRepository.findByActivationCode(invalidActivationCode)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.validateActivationCode(invalidActivationCode));
        verify(userRepository).findByActivationCode(invalidActivationCode);
        verifyNoMoreInteractions(userRepository);
    }
}
