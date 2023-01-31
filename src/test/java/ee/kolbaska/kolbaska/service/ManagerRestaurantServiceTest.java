package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.exception.UserAlreadyExistsException;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.user.Role;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.RestaurantRepository;
import ee.kolbaska.kolbaska.repository.RoleRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.WaiterRequest;
import ee.kolbaska.kolbaska.response.WaiterDeletedResponse;
import ee.kolbaska.kolbaska.response.WaiterResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.FormatService;
import ee.kolbaska.kolbaska.service.miscellaneous.PasswordService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import javax.management.relation.RoleNotFoundException;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource("/tests.properties")
public class ManagerRestaurantServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private FormatService formatService;

    @Mock
    private PasswordService passwordService;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private ManagerRestaurantService managerRestaurantService;


    @Test
    void testCreateWaiter() throws Exception {
        // mock the dependencies of the RestaurantService
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("R14");
        restaurant.setDescription("Zalupa konja");
        restaurant.setRestaurantCode("123456");

        when(restaurantRepository.findByRestaurantCode(anyString())).thenReturn(Optional.of(restaurant));
        User waiter = new User();
        waiter.setId(1L);
        waiter.setEmail("test@test.com");
        waiter.setFullName("John Doe");
        waiter.setPhone("+370000000");
        when(userRepository.save(any(User.class))).thenReturn(waiter);
        when(passwordService.generatePassword(anyInt())).thenReturn("password");
        when(formatService.formatE164(anyString())).thenReturn("+370000000");
        doNothing().when(emailService).sendSimpleMessage(anyString(), anyString(), anyString());
        Role role = new Role();
        role.setRoleName("ROLE_WAITER");
        when(roleRepository.findRoleByRoleName("ROLE_WAITER")).thenReturn(Optional.of(role));

        // create the request object
        WaiterRequest request = new WaiterRequest();
        request.setEmail("test@test.com");
        request.setFullName("John Doe");
        request.setPhone("0000000");
        request.setRestaurantCode("123456");

        // call the createWaiter method
        ResponseEntity<WaiterResponse> response = managerRestaurantService.createWaiter(request);

        // assert that the response is as expected
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId().longValue());
        assertEquals("test@test.com", response.getBody().getEmail());
        assertEquals("John Doe", response.getBody().getFullName());
        assertEquals("+370000000", response.getBody().getPhone());
        assertEquals(0.0, response.getBody().getTurnover(), 0.0);

        // verify that the dependencies were called as expected
        verify(passwordEncoder).encode("password");
        verify(userRepository).findByEmail("test@test.com");
        verify(userRepository).save(any(User.class));
        verify(passwordService).generatePassword(10);
        verify(formatService).formatE164("0000000");
        verify(emailService).sendSimpleMessage("test@test.com", "Password", "Here is your password for accessing qr code page: password");
        verify(roleRepository).findRoleByRoleName("ROLE_WAITER");
    }

    @Test
    void testCreateWaiter_UserAlreadyExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));


        WaiterRequest request = new WaiterRequest();
        request.setEmail("test@test.com");
        request.setFullName("John Doe");
        request.setPhone("0000000");

        assertThrows(UserAlreadyExistsException.class, () -> managerRestaurantService.createWaiter(request));
    }

    @Test
    void testCreateWaiter_RoleNotFound() {
        when(roleRepository.findRoleByRoleName("ROLE_WAITER")).thenReturn(Optional.empty());

        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("R14");
        restaurant.setDescription("Zalupa konja");
        restaurant.setRestaurantCode("123456");

        when(restaurantRepository.findByRestaurantCode(anyString())).thenReturn(Optional.of(restaurant));

        WaiterRequest request = new WaiterRequest();
        request.setEmail("test@test.com");
        request.setFullName("John Doe");
        request.setPhone("0000000");
        request.setRestaurantCode("123456");

        assertThrows(RoleNotFoundException.class, () -> managerRestaurantService.createWaiter(request));
    }

    @Test
    void testCreateWaiter_InvalidPhoneNumber() {
        doThrow(IllegalArgumentException.class).when(formatService).formatE164(anyString());

        WaiterRequest request = new WaiterRequest();
        request.setEmail("test@test.com");
        request.setFullName("John Doe");
        request.setPhone("invalid_number");

        assertThrows(IllegalArgumentException.class, () -> managerRestaurantService.createWaiter(request));
    }

    @Test
    void deleteWaiter_waiterExists_waiterIsDeleted() {
        User waiter = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("password")
                .build();

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(waiter));
        when(userRepository.save(any(User.class))).thenReturn(waiter);

        ResponseEntity<WaiterDeletedResponse> response = managerRestaurantService.deleteWaiter(1L);

        assertEquals(Objects.requireNonNull(response.getBody()).getId(), 1L);
        assertTrue(response.getBody().isDeleted());
        assertFalse(waiter.isAccountNonLocked());
        assertNotNull(waiter.getDeletedAt());
    }

    @Test
    void deleteWaiter_waiterNotExists_throwsUsernameNotFoundException() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> managerRestaurantService.deleteWaiter(1L));
    }
}