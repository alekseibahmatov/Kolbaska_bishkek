package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.config.UserConfiguration;
import ee.kolbaska.kolbaska.exception.RestaurantNotFoundException;
import ee.kolbaska.kolbaska.exception.UserStillOnDutyException;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.user.Role;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.AddressRepository;
import ee.kolbaska.kolbaska.repository.RestaurantRepository;
import ee.kolbaska.kolbaska.repository.RoleRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.WaiterRequest;
import ee.kolbaska.kolbaska.response.WaiterDeletedResponse;
import ee.kolbaska.kolbaska.response.WaiterResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.FormatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ManagerRestaurantServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserConfiguration userConfiguration;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FormatService formatService;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private ManagerRestaurantService managerRestaurantService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateWaiter() throws Exception {
        // Arrange
        WaiterRequest waiterRequest = WaiterRequest.builder()
                .email("waiter@example.com")
                .build();

        User manager = User.builder()
                .id(1L)
                .managedRestaurant(Restaurant.builder().id(2L).build())
                .build();

        when(userConfiguration.getRequestUser()).thenReturn(manager);
        when(userRepository.findByEmail(waiterRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.findRoleByRoleName("ROLE_WAITER")).thenReturn(Optional.of(Role.builder().id(3L).roleName("ROLE_WAITER").build()));
        when(roleRepository.findRoleByRoleName("ROLE_NEWBIE")).thenReturn(Optional.of(Role.builder().id(4L).roleName("ROLE_NEWBIE").build()));

        // Act
        WaiterResponse waiterResponse = managerRestaurantService.createWaiter(waiterRequest);

        // Assert
        assertNotNull(waiterResponse);
        verify(userRepository).findByEmail(waiterRequest.getEmail());
        verify(roleRepository, times(2)).findRoleByRoleName(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateWaiterExistingUser() {
        // Arrange
        WaiterRequest waiterRequest = WaiterRequest.builder()
                .email("waiter@example.com")
                .build();

        User manager = User.builder()
                .id(1L)
                .managedRestaurant(Restaurant.builder().id(2L).build())
                .build();

        User existingUser = User.builder().id(3L).restaurant(Restaurant.builder().id(4L).build()).build();
        when(userConfiguration.getRequestUser()).thenReturn(manager);
        when(userRepository.findByEmail(waiterRequest.getEmail())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(UserStillOnDutyException.class, () -> managerRestaurantService.createWaiter(waiterRequest));
        verify(userRepository).findByEmail(waiterRequest.getEmail());
    }

    @Test
    void testCreateWaiterNoManagedRestaurant() {
        // Arrange
        WaiterRequest waiterRequest = WaiterRequest.builder()
                .email("waiter@example.com")
                .build();
        User manager = User.builder().id(1L).build();
        when(userConfiguration.getRequestUser()).thenReturn(manager);

        // Act & Assert
        assertThrows(RestaurantNotFoundException.class, () -> managerRestaurantService.createWaiter(waiterRequest));
    }

    @Test
    public void testDeleteWaiter() {

        Long waiterId = 1L;

        // Arrange
        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .build();

        User manager = new User();
        manager.setManagedRestaurant(restaurant);

        User waiter = new User();
        waiter.setId(waiterId);

        restaurant.setWaiters(List.of(waiter));

        Optional<User> optionalWaiter = Optional.of(waiter);

        when(userRepository.findById(waiterId)).thenReturn(optionalWaiter);
        when(userConfiguration.getRequestUser()).thenReturn(manager);
        when(restaurantRepository.findById(manager.getManagedRestaurant().getId())).thenReturn(Optional.of(manager.getManagedRestaurant()));

        // Act
        WaiterDeletedResponse response = managerRestaurantService.deleteWaiter(waiterId);

        // Assert
        assertTrue(response.isDeleted());
        assertEquals(waiterId, response.getId());
        assertNull(waiter.getRestaurant());
        verify(userRepository).findById(waiterId);
        verify(userRepository).save(waiter);
    }

    @Test
    public void testDeleteWaiterNotFound() {
        // Arrange
        Long waiterId = 1L;
        when(userRepository.findById(waiterId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> managerRestaurantService.deleteWaiter(waiterId));
        verify(userRepository).findById(waiterId);
    }

    @Test
    public void testDeleteWaiterWrongRestaurant() {
        // Arrange
        Long waiterId = 1L;
        User manager = new User();
        manager.setManagedRestaurant(new Restaurant());

        User waiter = new User();
        waiter.setId(waiterId);
        waiter.setRestaurant(new Restaurant());

        Optional<User> optionalWaiter = Optional.of(waiter);

        when(userRepository.findById(waiterId)).thenReturn(optionalWaiter);
        when(userConfiguration.getRequestUser()).thenReturn(manager);
        when(restaurantRepository.findById(manager.getManagedRestaurant().getId())).thenReturn(Optional.of(manager.getManagedRestaurant()));

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> managerRestaurantService.deleteWaiter(waiterId));
        verify(userRepository).findById(waiterId);
    }
}