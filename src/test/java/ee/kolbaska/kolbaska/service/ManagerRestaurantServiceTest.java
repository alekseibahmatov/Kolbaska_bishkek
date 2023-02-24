package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.config.UserConfiguration;
import ee.kolbaska.kolbaska.exception.RestaurantNotFoundException;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.transaction.Transaction;
import ee.kolbaska.kolbaska.model.user.Role;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.RestaurantRepository;
import ee.kolbaska.kolbaska.repository.RoleRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.WaiterRequest;
import ee.kolbaska.kolbaska.response.WaiterDeletedResponse;
import ee.kolbaska.kolbaska.response.WaiterResponse;
import ee.kolbaska.kolbaska.security.JwtService;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.FormatService;
import ee.kolbaska.kolbaska.service.miscellaneous.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.management.relation.RoleNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    @Mock
    private JwtService jwtService;

    @Mock
    private UserConfiguration userConfiguration;

    @InjectMocks
    private ManagerRestaurantService managerRestaurantService;


    @Test
    void testCreateWaiter() throws Exception {
        // mock the dependencies of the RestaurantService
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.findByPersonalCode(anyString())).thenReturn(Optional.empty());

        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("R14");
        restaurant.setDescription("Zalupa konja");
        restaurant.setRestaurantCode("123456");

        when(restaurantRepository.findByRestaurantCode(anyString())).thenReturn(Optional.of(restaurant));
        User waiter = new User();
        waiter.setId(1L);
        waiter.setEmail("test@test.com");
        waiter.setPersonalCode("12345612345");
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
        request.setPersonalCode("12345612345");
        request.setRestaurantCode("123456");

        // call the createWaiter method
        WaiterResponse response = managerRestaurantService.createWaiter(request);

        // assert that the response is as expected
        assertNotNull(response);
        assertEquals(1L, response.getId().longValue());
        assertEquals("test@test.com", response.getEmail());
        assertEquals("John Doe", response.getFullName());
        assertEquals("+370000000", response.getPhone());
        assertEquals(0.0, response.getTurnover(), 0.0);

        // verify that the dependencies were called as expected
        verify(passwordEncoder).encode("password");
        verify(userRepository).findByPersonalCode("12345612345");
        verify(userRepository).save(any(User.class));
        verify(passwordService).generatePassword(10);
        verify(formatService).formatE164("0000000");
        verify(emailService).sendSimpleMessage("test@test.com", "Password", "Here is your password for accessing qr code page: password");
        verify(roleRepository).findRoleByRoleName("ROLE_WAITER");
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
    void createWaiter_waiterAlreadyExists_waiterIsReassignedToRestaurant() throws Exception {
        WaiterRequest request = WaiterRequest.builder()
                .fullName("Test Waiter")
                .email("test@test.com")
                .personalCode("ABC123")
                .restaurantCode("RES123")
                .phone("1234567890")
                .build();

        Role role = Role.builder()
                .id(1L)
                .roleName("ROLE_WAITER")
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .restaurantCode("RES123")
                .build();

        User waiter = User.builder()
                .id(1L)
                .fullName("Test Waiter")
                .email("test@test.com")
                .phone("1234567890")
                .password("password")
                .role(role)
                .restaurant(null)
                .build();

        when(userRepository.findByPersonalCode(anyString())).thenReturn(Optional.of(waiter));
        when(restaurantRepository.findByRestaurantCode(anyString())).thenReturn(Optional.of(restaurant));

        WaiterResponse response = managerRestaurantService.createWaiter(request);

        assertEquals(response.getId(), 1L);
        assertEquals(response.getEmail(), "test@test.com");
        assertEquals(response.getFullName(), "Test Waiter");
        assertEquals(response.getPhone(), "1234567890");
        assertEquals(response.getTurnover(), 0.0, 0.0);
        assertEquals(waiter.getRestaurant(), restaurant);
    }

    @Test
    public void deleteWaiter_waiterExists_waiterDeleted() {
        Long id = 1L;
        Role role = Role.builder()
                .id(1L)
                .roleName("ROLE_WAITER")
                .build();
        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .restaurantCode("RES123")
                .build();
        User waiter = User.builder()
                .id(id)
                .fullName("Test Waiter")
                .email("test@test.com")
                .password("password")
                .role(role)
                .restaurant(restaurant)
                .build();

        restaurant.setWaiters(List.of(waiter));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(waiter));
        when(userRepository.save(any(User.class))).thenReturn(User.builder().id(id).deleted(true).build());
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(Restaurant.builder().id(1L).build());

        WaiterDeletedResponse response = managerRestaurantService.deleteWaiter(id);

        assertEquals(response.getId(), id);
        assertTrue(response.isDeleted());
    }

    @Test
    void deleteWaiter_waiterNotExists_throwsUsernameNotFoundException() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> managerRestaurantService.deleteWaiter(1L));
    }

    @Test
    public void getWaiters_SuccessfulScenario_ShouldReturnListOfWaiters() throws RestaurantNotFoundException {

        String email = "test@test.com";

        User manager = User.builder()
                .id(1L)
                .email(email)
                .fullName("Test Test")
                .build();
        when(userConfiguration.getRequestUser()).thenReturn(manager);

        Restaurant restaurant = Restaurant.builder().id(1L).build();
        manager.setRestaurant(restaurant);

        User waiter1 = User.builder()
                .id(2L)
                .email("waiter1@test.com")
                .fullName("Waiter1 Test")
                .phone("111-111-1111")
                .restaurant(restaurant)
                .build();
        User waiter2 = User.builder()
                .id(3L)
                .email("waiter2@test.com")
                .fullName("Waiter2 Test")
                .phone("222-222-2222")
                .restaurant(restaurant)
                .build();
        List<User> waiters = Arrays.asList(waiter1, waiter2);
        restaurant.setWaiters(waiters);

        Transaction transaction1 = Transaction.builder().value(100.0).waiter(waiter1).build();
        Transaction transaction2 = Transaction.builder().value(200.0).waiter(waiter1).build();
        Transaction transaction3 = Transaction.builder().value(500.0).waiter(waiter2).build();
        waiter1.setTransactions(Arrays.asList(transaction1, transaction2));
        waiter2.setTransactions(Collections.singletonList(transaction3));

        List<WaiterResponse> result = managerRestaurantService.getWaiters();

        assertEquals(2, result.size());
        WaiterResponse waiterResponse1 = result.get(0);
        WaiterResponse waiterResponse2 = result.get(1);
        assertEquals(2L, waiterResponse1.getId());
        assertEquals("Waiter1 Test", waiterResponse1.getFullName());
        assertEquals("111-111-1111", waiterResponse1.getPhone());
        assertEquals("waiter1@test.com", waiterResponse1.getEmail());
        assertEquals(300.0, waiterResponse1.getTurnover());
        assertEquals(500.0, waiterResponse2.getTurnover());
    }

    @Test
    void whenGetWaitersAndRestaurantNotFound_thenReturnRestaurantNotFoundException() {

        String email = "test@email.com";

        User manager = User.builder().email(email).restaurant(null).build();
        when(userConfiguration.getRequestUser()).thenReturn(manager);

        Assertions.assertThrows(RestaurantNotFoundException.class, () -> managerRestaurantService.getWaiters());
    }

}