package ee.maitsetuur.service;

import ee.maitsetuur.config.UserConfiguration;
import ee.maitsetuur.exception.RestaurantNotFoundException;
import ee.maitsetuur.exception.UserStillOnDutyException;
import ee.maitsetuur.mapper.AddressMapper;
import ee.maitsetuur.model.address.Address;
import ee.maitsetuur.model.restaurant.Restaurant;
import ee.maitsetuur.model.user.Role;
import ee.maitsetuur.model.user.User;
import ee.maitsetuur.repository.AddressRepository;
import ee.maitsetuur.repository.RestaurantRepository;
import ee.maitsetuur.repository.RoleRepository;
import ee.maitsetuur.repository.UserRepository;
import ee.maitsetuur.request.AddressRequest;
import ee.maitsetuur.request.ManagerCustomerUpdateRequest;
import ee.maitsetuur.request.WaiterRequest;
import ee.maitsetuur.response.CustomerInformationResponse;
import ee.maitsetuur.response.CustomerUpdateResponse;
import ee.maitsetuur.response.WaiterDeletedResponse;
import ee.maitsetuur.response.WaiterResponse;
import ee.maitsetuur.service.miscellaneous.EmailService;
import ee.maitsetuur.service.miscellaneous.FormatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestPropertySource("/tests.properties")

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

        Restaurant managedRestaurant = Restaurant.builder().build();
        managedRestaurant.setId(2L);

        User manager = User.builder()
                .managedRestaurant(managedRestaurant)
                .build();
        manager.setId(1L);

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

        Restaurant managedRestaurant = Restaurant.builder().build();
        managedRestaurant.setId(2L);

        User manager = User.builder()
                .managedRestaurant(managedRestaurant)
                .build();
        manager.setId(1L);

        Restaurant anotherRestaurant = Restaurant.builder().build();
        anotherRestaurant.setId(4L);



        User existingUser = User.builder()
                .restaurant(anotherRestaurant)
                .build();
        existingUser.setId(2L);

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
        User manager = User.builder().build();
        manager.setId(1L);

        when(userConfiguration.getRequestUser()).thenReturn(manager);

        // Act & Assert
        assertThrows(RestaurantNotFoundException.class, () -> managerRestaurantService.createWaiter(waiterRequest));
    }

    @Test
    public void testDeleteWaiter() {

        Long waiterId = 1L;

        // Arrange
        Restaurant restaurant = Restaurant.builder().build();
        restaurant.setId(1L);

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

    @Test
    void testGetWaitersSuccess() throws RestaurantNotFoundException {
        // Arrange
        User manager = new User();
        Restaurant restaurant = new Restaurant();

        User user1 = User.builder().fullName("John Doe").email("john@example.com").phone("+37254535251").build();
        user1.setId(1L);

        User user2 = User.builder().fullName("Jane Smith").email("jane@example.com").phone("+37254535252").build();
        user2.setId(2L);

        restaurant.setWaiters(List.of(user1, user2));

        manager.setManagedRestaurant(restaurant);
        when(userConfiguration.getRequestUser()).thenReturn(manager);

        // Act
        List<WaiterResponse> response = managerRestaurantService.getWaiters();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals("John Doe", response.get(0).getFullName());
        assertEquals("john@example.com", response.get(0).getEmail());
        assertEquals("+37254535251", response.get(0).getPhone());
        assertEquals(0.0, response.get(0).getTurnover());
        assertEquals(2L, response.get(1).getId());
        assertEquals("Jane Smith", response.get(1).getFullName());
        assertEquals("jane@example.com", response.get(1).getEmail());
        assertEquals("+37254535252", response.get(1).getPhone());
        assertEquals(0.0, response.get(1).getTurnover());
    }

    @Test
    void testGetWaitersException() {
        // Arrange
        User manager = new User();
        manager.setManagedRestaurant(null);
        when(userConfiguration.getRequestUser()).thenReturn(manager);

        // Act & Assert
        RestaurantNotFoundException exception = assertThrows(RestaurantNotFoundException.class, () -> managerRestaurantService.getWaiters());
        assertEquals("This manager is not associated with any restaurant!", exception.getMessage());
    }

    @Test
    void testGetWaiterSuccessfully() {
        // Arrange
        User manager = new User();
        manager.setManagedRestaurant(new Restaurant());

        User waiter = User.builder()
                .fullName("John Doe")
                .email("johndoe@example.com")
                .phone("+37256545351")
                .personalCode("12345678901")
                .address(Address.builder().city("Rio-De-Zanejro").country("Brazil").state("Harjumaa").street("Kiikri 2/3").apartmentNumber("103").zipCode("123456").build())
                .transactions(Collections.emptyList())
                .logins(Collections.emptyList())
                .build();
        waiter.setId(1L);

        Restaurant restaurant = manager.getManagedRestaurant();
        restaurant.setWaiters(List.of(waiter));

        Mockito.when(userConfiguration.getRequestUser()).thenReturn(manager);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(waiter));

        // Act
        CustomerInformationResponse response = managerRestaurantService.getWaiter(1L);

        // Assert
        assertNotNull(response);
        assertEquals("John Doe", response.getFullName());
        assertEquals("johndoe@example.com", response.getEmail());
        assertEquals("+37256545351", response.getPhone());
        assertEquals("12345678901", response.getPersonalCode());
        assertNotNull(response.getAddress());
        assertEquals("Rio-De-Zanejro", response.getAddress().getCity());
        assertEquals("Brazil", response.getAddress().getCountry());
        assertEquals("Kiikri 2/3", response.getAddress().getStreet());
        assertNotNull(response.getTransactions());
        assertEquals(0, response.getTransactions().size());
        assertNotNull(response.getLogins());
        assertEquals(0, response.getLogins().size());
    }

    @Test
    void testGetWaiterNotFoundById() {
        // Arrange
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> managerRestaurantService.getWaiter(1L));
    }

    @Test
    void testGetWaiterNotAssociatedWithManagerRestaurant() {
        // Arrange
        User manager = new User();
        manager.setManagedRestaurant(new Restaurant());

        User waiter = new User();
        waiter.setId(1L);

        Mockito.when(userConfiguration.getRequestUser()).thenReturn(manager);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(waiter));

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> managerRestaurantService.getWaiter(1L));
    }

    @Test
    void testUpdateWaiterSuccess() throws UsernameNotFoundException {
        // Arrange
        ManagerCustomerUpdateRequest request = ManagerCustomerUpdateRequest.builder()
                .id(1L)
                .fullName("John Smith")
                .newPassword("new_password")
                .phone("123456789")
                .personalCode("123456789")
                .address(AddressRequest.builder().city("New York").street("5th Avenue").build())
                .email("john.smith@example.com")
                .build();

        User user = User.builder()
                .email("john.smith@example.com")
                .roles(List.of(Role.builder().roleName("ROLE_WAITER").build()))
                .fullName("John")
                .phone("123456789")
                .personalCode("123456789")
                .address(Address.builder().city("New York").street("4th Avenue").build())
                .build();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userConfiguration.getRequestUser()).thenReturn(User.builder().managedRestaurant(Restaurant.builder().waiters(List.of(user)).build()).build());
        when(addressRepository.save(any(Address.class))).thenReturn(AddressMapper.INSTANCE.toAddress(request.getAddress()));

        // Act
        CustomerUpdateResponse response = managerRestaurantService.updateWaiter(request);

        // Assert
        assertNotNull(response);
        assertEquals("User was successfully updated!", response.getMessage());
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateWaiterUserNotFound() {
        // Arrange
        ManagerCustomerUpdateRequest request = ManagerCustomerUpdateRequest.builder()
                .id(1L)
                .fullName("John Smith")
                .newPassword("new_password")
                .phone("123456789")
                .personalCode("123456789")
                .address(AddressRequest.builder().city("New York").street("5th Avenue").build())
                .email("john.smith@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> managerRestaurantService.updateWaiter(request));
        verify(userRepository).findById(1L);
    }

    @Test
    void testUpdateWaiterWaiterNotFound() {
        // Arrange
        ManagerCustomerUpdateRequest request = ManagerCustomerUpdateRequest.builder()
                .id(1L)
                .fullName("John Smith")
                .newPassword("new_password")
                .phone("123456789")
                .personalCode("123456789")
                .address(AddressRequest.builder().city("New York").street("5th Avenue").build())
                .email("john.smith@example.com")
                .build();

        User user = User.builder()
                .email("john.smith@example.com")
                .roles(List.of(Role.builder().roleName("ROLE_WAITER").build()))
                .fullName("John")
                .phone("123456789")
                .personalCode("123456789")
                .address(Address.builder().city("New York").street("4th Avenue").build())
                .build();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userConfiguration.getRequestUser()).thenReturn(User.builder().managedRestaurant(Restaurant.builder().waiters(Collections.emptyList()).build()).build());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> managerRestaurantService.updateWaiter(request));
        verify(userRepository).findById(1L);
    }

}