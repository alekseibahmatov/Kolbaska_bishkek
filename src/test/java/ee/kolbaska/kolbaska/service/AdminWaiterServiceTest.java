package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.config.UserConfiguration;
import ee.kolbaska.kolbaska.model.address.Address;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.transaction.Transaction;
import ee.kolbaska.kolbaska.model.user.Role;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.AddressRepository;
import ee.kolbaska.kolbaska.repository.RoleRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.AddressRequest;
import ee.kolbaska.kolbaska.request.AdminCustomerUpdateRequest;
import ee.kolbaska.kolbaska.response.CustomerInformationResponse;
import ee.kolbaska.kolbaska.response.CustomerUpdateResponse;
import ee.kolbaska.kolbaska.response.WaiterResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.FormatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import javax.management.relation.RoleNotFoundException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestPropertySource("/tests.properties")
class AdminWaiterServiceTest {

    @Mock
    private UserConfiguration userConfiguration;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FormatService formatService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AdminWaiterService adminWaiterService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetWaiter_Success() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("johndoe@example.com")
                .phone("123456789")
                .personalCode("1234")
                .activated(true)
                .deleted(false)
                .activationCode("xyz")
                .address(Address.builder().build())
                .restaurant(Restaurant.builder().id(1L).restaurantCode("ABC").build())
                .transactions(Collections.emptyList())
                .logins(Collections.emptyList())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        CustomerInformationResponse response = adminWaiterService.getWaiter(1L);

        // Assert
        assertNotNull(response);
        assertEquals("John Doe", response.getFullName());
        assertEquals("johndoe@example.com", response.getEmail());
        assertEquals("123456789", response.getPhone());

        assertNotNull(response.getAddress());
        assertEquals("1234", response.getPersonalCode());
        assertTrue(response.getActivated());
        assertFalse(response.getDeleted());

        assertEquals("xyz", response.getActivationCode());
        assertEquals("ABC", response.getRestaurantCode());
        assertNotNull(response.getTransactions());
        assertTrue(response.getTransactions().isEmpty());

        assertNotNull(response.getLogins());
        assertTrue(response.getLogins().isEmpty());
        assertNotNull(response.getRoleNames());
        assertTrue(response.getRoleNames().isEmpty());

        verify(userRepository).findById(1L);
    }

    @Test
    void testGetWaiter_Exception() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> adminWaiterService.getWaiter(1L));
        verify(userRepository).findById(1L);
    }

    @Test
    void testUpdateWaiterSuccess() throws RoleNotFoundException, UsernameNotFoundException {
        // Arrange
        Long id = 1L;
        String fullName = "John Doe";
        String newPassword = "password";
        String phone = "+37253545556";
        String personalCode = "12345";
        String email = "johndoe@example.com";
        List<String> roleNames = List.of("ROLE_WAITER");
        boolean activated = true;
        boolean deleted = false;
        String activationCode = "abc123";

        User user = User.builder()
                .id(id)
                .fullName("Jane Doe")
                .email("janedoe@example.com")
                .build();

        AdminCustomerUpdateRequest request = AdminCustomerUpdateRequest.builder()
                .id(id)
                .fullName(fullName)
                .newPassword(newPassword)
                .phone(phone)
                .personalCode(personalCode)
                .address(AddressRequest.builder().build())
                .email(email)
                .roleNames(roleNames)
                .activated(activated)
                .deleted(deleted)
                .activationCode(activationCode)
                .build();

        Role role = Role.builder().roleName("ROLE_WAITER").build();
        List<Role> roles = new ArrayList<>();
        roles.add(role);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(formatService.formatE164(phone)).thenReturn(phone);
        when(passwordEncoder.encode(anyString())).thenReturn("password");
        when(roleRepository.findRoleByRoleName("ROLE_WAITER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CustomerUpdateResponse response = adminWaiterService.updateWaiter(request);

        // Assert
        assertNotNull(response);
        assertEquals("User was successfully updated!", response.getMessage());
        assertEquals(fullName, user.getFullName());
        assertEquals(newPassword, user.getPassword());

        assertEquals(phone, user.getPhone());
        assertEquals(personalCode, user.getPersonalCode());
        assertEquals(email, user.getEmail());
        assertEquals(activated, user.getActivated());

        assertEquals(deleted, user.getDeleted());
        assertEquals(activationCode, user.getActivationCode());
        assertEquals(roles, user.getRoles());
    }

    @Test
    void testUpdateWaiterRoleNotFound() {
        // Arrange
        Long id = 1L;
        List<String> roleNames = List.of("ROLE_WAITER");

        AdminCustomerUpdateRequest request = AdminCustomerUpdateRequest.builder()
                .id(id)
                .roleNames(roleNames)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(new User()));
        when(roleRepository.findRoleByRoleName("ROLE_WAITER")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RoleNotFoundException.class, () -> adminWaiterService.updateWaiter(request));
    }

    @Test
    void testUpdateWaiterUserNotFound() {
        // Arrange
        Long id = 1L;

        AdminCustomerUpdateRequest request = AdminCustomerUpdateRequest.builder()
                .id(id)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> adminWaiterService.updateWaiter(request));
    }

    @Test
    void testGetWaiters_ReturnsWaiterResponseList() {
        // Mock repository to return a list of waiters
        List<User> waiters = new ArrayList<>();
        User waiter1 = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .build();

        Transaction transaction1 = Transaction.builder().value(10.0).build();
        Transaction transaction2 = Transaction.builder().value(15.0).build();

        waiter1.setTransactions(Arrays.asList(transaction1, transaction2));

        waiters.add(waiter1);

        User waiter2 = User.builder()
                .id(2L)
                .fullName("Jane Doe")
                .email("jane.doe@example.com")
                .phone("+9876543210")
                .build();

        Transaction transaction3 = Transaction.builder().value(20.0).build();

        waiter2.setTransactions(Collections.singletonList(transaction3));

        waiters.add(waiter2);

        when(userRepository.findUsersByRestaurantIsNotNull()).thenReturn(waiters);

        // Call the method
        List<WaiterResponse> response = adminWaiterService.getWaiters();

        // Verify the response
        assertEquals(2, response.size());
        WaiterResponse waiterResponse1 = response.get(0);
        assertEquals(1L, waiterResponse1.getId());
        assertEquals("John Doe", waiterResponse1.getFullName());

        assertEquals("john.doe@example.com", waiterResponse1.getEmail());
        assertEquals("+1234567890", waiterResponse1.getPhone());
        assertEquals(25.0, waiterResponse1.getTurnover());
        WaiterResponse waiterResponse2 = response.get(1);

        assertEquals(2L, waiterResponse2.getId());
        assertEquals("Jane Doe", waiterResponse2.getFullName());
        assertEquals("jane.doe@example.com", waiterResponse2.getEmail());
        assertEquals("+9876543210", waiterResponse2.getPhone());

        assertEquals(20.0, waiterResponse2.getTurnover());
    }

}