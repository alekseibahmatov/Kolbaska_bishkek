package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.model.user.Role;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.RoleRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.*;
import ee.kolbaska.kolbaska.response.AuthenticationResponse;
import ee.kolbaska.kolbaska.response.PersonalDataResponse;
import ee.kolbaska.kolbaska.response.RecoveryResponse;
import ee.kolbaska.kolbaska.security.JwtService;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.FormatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;


import javax.management.relation.RoleNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    @Mock
    private FormatService formatService;
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
        user.setRoles(List.of(roleRepository.findRoleByRoleName("ROLE_CUSTOMER").get()));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtService.createToken(anyMap(), any(User.class))).thenReturn("token");

        UserAuthenticationRequest userAuthenticationRequest = new UserAuthenticationRequest();
        userAuthenticationRequest.setEmail("test@test.com");
        userAuthenticationRequest.setPassword("password");

        AuthenticationResponse response = authenticationService.authenticate(userAuthenticationRequest);
        assertEquals("token", response.getToken());
    }

    @Test
    void testAuthenticate_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        UserAuthenticationRequest userAuthenticationRequest = new UserAuthenticationRequest();
        userAuthenticationRequest.setEmail("test@test.com");
        userAuthenticationRequest.setPassword("password");

        assertThrows(NoSuchElementException.class, () -> authenticationService.authenticate(userAuthenticationRequest));
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

    @Test
    @Transactional
    void testSavePersonalData() {
        // Set up test data
        String activationCode = "123456";
        String personalCode = "123456789";
        String fullName = "John Doe";
        String password = "password";
        AddressRequest address = new AddressRequest("123 Main St", "Apt 1", "New York", "NY", "10001", "USA");
        String phone = "+1 (123) 456-7890";

        Role roleWaiter = Role.builder()
                .roleName("ROLE_WAITER")
                .build();
        Role roleNewbie = Role.builder()
                .roleName("ROLE_NEWBIE")
                .build();

        User user = new User();
        user.setActivationCode(activationCode);
        user.setRoles(List.of(roleWaiter, roleNewbie));

        when(userRepository.findByActivationCode(activationCode)).thenReturn(Optional.of(user));

        PersonalDataRequest request = PersonalDataRequest.builder()
                .activationCode(activationCode)
                .personalCode(personalCode)
                .fullName(fullName)
                .password(password)
                .address(address)
                .phone(phone)
                .build();

        // Call the method being tested
        PersonalDataResponse response = authenticationService.savePersonalData(request);

        // Assert that the personal data was saved
        assertTrue(response.getMessage().contains("successfully saved"));
        User savedUser = userRepository.findByActivationCode(activationCode).orElse(null);
        assertNotNull(savedUser);
        assertEquals(personalCode, savedUser.getPersonalCode());
        assertEquals(fullName, savedUser.getFullName());
        assertNotNull(savedUser.getAddress());
        assertEquals(address.getStreet(), savedUser.getAddress().getStreet());
        assertEquals(address.getApartmentNumber(), savedUser.getAddress().getApartmentNumber());
        assertEquals(address.getCity(), savedUser.getAddress().getCity());
        assertEquals(address.getState(), savedUser.getAddress().getState());
        assertEquals(address.getZipCode(), savedUser.getAddress().getZipCode());
        assertEquals(address.getCountry(), savedUser.getAddress().getCountry());
        assertEquals(formatService.formatE164(phone), savedUser.getPhone());
        assertNull(savedUser.getActivationCode());
    }

}

