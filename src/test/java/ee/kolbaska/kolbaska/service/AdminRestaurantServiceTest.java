package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.exception.RestaurantAlreadyExistsException;
import ee.kolbaska.kolbaska.exception.RestaurantNotFoundException;
import ee.kolbaska.kolbaska.exception.UserStillOnDutyException;
import ee.kolbaska.kolbaska.mapper.AddressMapper;
import ee.kolbaska.kolbaska.model.address.Address;
import ee.kolbaska.kolbaska.model.category.Category;
import ee.kolbaska.kolbaska.model.file.File;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.*;
import ee.kolbaska.kolbaska.request.AddressRequest;
import ee.kolbaska.kolbaska.request.RestaurantRequest;
import ee.kolbaska.kolbaska.request.RestaurantUpdateRequest;
import ee.kolbaska.kolbaska.response.RestaurantDisableResponse;
import ee.kolbaska.kolbaska.response.RestaurantResponse;
import ee.kolbaska.kolbaska.response.RestaurantTableResponse;
import ee.kolbaska.kolbaska.response.RestaurantUpdateResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;


@TestPropertySource("/tests.properties")
class AdminRestaurantServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AdminRestaurantService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Transactional
    public void testCreateRestaurantSuccessNoManagerAccountCreated() throws Exception {
        // arrange

        List<String> categories = List.of("China", "Hululo");

        MockMultipartFile photo = new MockMultipartFile("image.png", "image".getBytes());
        MockMultipartFile contract = new MockMultipartFile("contract.pdf", "contract".getBytes());

        RestaurantRequest request = RestaurantRequest.builder()
                .restaurantName("McDonalds")
                .restaurantDescription("Best restaurant")
                .restaurantEmail("test@test.com")
                .managerEmail("manager@test.com")
                .restaurantPhone("+37258535156")
                .country("Estonia")
                .city("Tallinn")
                .state("Harjumaa")
                .street("Zalupka 1/2")
                .postalCode("123456")
                .averageBill(120)
                .workingHours("24/7")
                .categories(categories)
                .photo(photo)
                .contract(contract)
                .build();

        Address address = Address.builder()
                .id(1L)
                .country(request.getCountry())
                .city(request.getCity())
                .state(request.getState())
                .street(request.getStreet())
                .build();

        Category category1 = Category.builder()
                .id(1L)
                .name(categories.get(0))
                .build();

        Category category2 = Category.builder()
                .id(2L)
                .name(categories.get(1))
                .build();

        User manager = User.builder()
                .email(request.getManagerEmail())
                .activationCode(UUID.randomUUID().toString())
                .build();

        Restaurant restaurant = Restaurant.builder()
                .name(request.getRestaurantName())
                .description(request.getRestaurantDescription())
                .email(request.getRestaurantEmail())
                .address(address)
                .averageBill(request.getAverageBill())
                .categories(List.of(category1, category2))
                .workingHours(request.getWorkingHours())
                .phone(request.getRestaurantPhone())
                .active(true)
                .restaurantCode(UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .manager(manager)
                .build();

        when(restaurantRepository.findByEmail(request.getRestaurantEmail())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getManagerEmail())).thenReturn(Optional.empty());
        when(addressRepository.save(any())).thenReturn(address);
        when(userRepository.save(any(User.class))).thenReturn(manager);
        when(restaurantRepository.save(any())).thenReturn(restaurant);

        // act
        RestaurantTableResponse response = service.createRestaurant(request);

        // assert
        assertNotNull(response);
        assertEquals(request.getRestaurantName(), response.getRestaurantName());
        assertEquals(request.getRestaurantEmail(), response.getRestaurantEmail());
        assertEquals(request.getRestaurantPhone(), response.getRestaurantPhone());
        assertEquals(request.getAverageBill(), response.getAverageBill());
        // add more assertions based on the expected result
    }

    @Test
    @Transactional
    public void testCreateRestaurantSuccessManagerAccountCreated() throws Exception {
        // arrange

        List<String> categories = List.of("China", "Hululo");

        MockMultipartFile photo = new MockMultipartFile("image.png", "image".getBytes());
        MockMultipartFile contract = new MockMultipartFile("contract.pdf", "contract".getBytes());

        RestaurantRequest request = RestaurantRequest.builder()
                .restaurantName("McDonalds")
                .restaurantDescription("Best restaurant")
                .restaurantEmail("test@test.com")
                .managerEmail("manager@test.com")
                .restaurantPhone("+37258535156")
                .country("Estonia")
                .city("Tallinn")
                .state("Harjumaa")
                .street("Zalupka 1/2")
                .postalCode("123456")
                .averageBill(120)
                .workingHours("24/7")
                .categories(categories)
                .photo(photo)
                .contract(contract)
                .build();

        Address address = Address.builder()
                .id(1L)
                .country(request.getCountry())
                .city(request.getCity())
                .state(request.getState())
                .street(request.getStreet())
                .build();

        Category category1 = Category.builder()
                .id(1L)
                .name(categories.get(0))
                .build();

        Category category2 = Category.builder()
                .id(2L)
                .name(categories.get(1))
                .build();

        User manager = User.builder()
                .email(request.getManagerEmail())
                .activationCode(UUID.randomUUID().toString())
                .activated(true)
                .deleted(false)
                .build();

        Restaurant restaurant = Restaurant.builder()
                .name(request.getRestaurantName())
                .description(request.getRestaurantDescription())
                .email(request.getRestaurantEmail())
                .address(address)
                .averageBill(request.getAverageBill())
                .categories(List.of(category1, category2))
                .workingHours(request.getWorkingHours())
                .phone(request.getRestaurantPhone())
                .active(true)
                .restaurantCode(UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .manager(manager)
                .build();

        when(restaurantRepository.findByEmail(request.getRestaurantEmail())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getManagerEmail())).thenReturn(Optional.of(manager));
        when(addressRepository.save(any())).thenReturn(address);
        when(userRepository.save(any(User.class))).thenReturn(manager);
        when(restaurantRepository.save(any())).thenReturn(restaurant);

        // act
        RestaurantTableResponse response = service.createRestaurant(request);

        // assert
        assertNotNull(response);
        assertEquals(request.getRestaurantName(), response.getRestaurantName());
        assertEquals(request.getRestaurantEmail(), response.getRestaurantEmail());
        assertEquals(request.getRestaurantPhone(), response.getRestaurantPhone());
        assertEquals(request.getAverageBill(), response.getAverageBill());
        // add more assertions based on the expected result
    }

    @Test
    @Transactional
    public void testCreateRestaurantFailedManagerAccountOnDuty() throws Exception {
        List<String> categories = List.of("China", "Hululo");

        MockMultipartFile photo = new MockMultipartFile("image.png", "image".getBytes());
        MockMultipartFile contract = new MockMultipartFile("contract.pdf", "contract".getBytes());

        RestaurantRequest request = RestaurantRequest.builder()
                .restaurantName("McDonalds")
                .restaurantDescription("Best restaurant")
                .restaurantEmail("test@test.com")
                .managerEmail("manager@test.com")
                .restaurantPhone("+37258535156")
                .country("Estonia")
                .city("Tallinn")
                .state("Harjumaa")
                .street("Zalupka 1/2")
                .postalCode("123456")
                .averageBill(120)
                .workingHours("24/7")
                .categories(categories)
                .photo(photo)
                .contract(contract)
                .build();

        Restaurant restaurant = Restaurant.builder()
                .name(request.getRestaurantName())
                .description(request.getRestaurantDescription())
                .email(request.getRestaurantEmail())
                .averageBill(request.getAverageBill())
                .workingHours(request.getWorkingHours())
                .phone(request.getRestaurantPhone())
                .active(true)
                .restaurantCode(UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .build();

        User manager = User.builder()
                .email(request.getManagerEmail())
                .activationCode(UUID.randomUUID().toString())
                .activated(true)
                .deleted(false)
                .managedRestaurant(restaurant)
                .build();

        when(restaurantRepository.findByEmail(request.getRestaurantEmail())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getManagerEmail())).thenReturn(Optional.of(manager));

        assertThrows(UserStillOnDutyException.class, () -> {
           service.createRestaurant(request);
        });
    }

    @Test
    @Transactional
    public void testCreateRestaurantAlreadyExists() {
        // arrange
        RestaurantRequest request = new RestaurantRequest();
        when(restaurantRepository.findByEmail(request.getRestaurantEmail())).thenReturn(Optional.of(new Restaurant()));

        // assert
        assertThrows(RestaurantAlreadyExistsException.class, () -> {
            // act
            service.createRestaurant(request);
        });
    }

    @Test
    public void testReturnRestaurantSuccess() throws Exception {
        // arrange
        String code = "ABC123";
        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .name("McDonalds")
                .email("mcdonalds@test.com")
                .phone("+37258535156")
                .description("Best restaurant")
                .manager(User.builder().id(1L).build())
                .categories(List.of(Category.builder().name("Fast food").build()))
                .workingHours("24/7")
                .averageBill(10)
                .address(Address.builder().id(1L).build())
                .photo(File.builder().id(UUID.randomUUID().toString()).build())
                .contract(File.builder().id(UUID.randomUUID().toString()).build())
                .active(true)
                .restaurantCode(code)
                .build();

        when(restaurantRepository.findByRestaurantCode(code)).thenReturn(Optional.of(restaurant));

        // act
        RestaurantResponse response = service.returnRestaurant(code);

        // assert
        assertNotNull(response);
        assertEquals(restaurant.getName(), response.getRestaurantName());
        assertEquals(restaurant.getEmail(), response.getRestaurantEmail());
        assertEquals(restaurant.getPhone(), response.getRestaurantPhone());
        assertEquals(restaurant.getDescription(), response.getRestaurantDescription());
        assertEquals(restaurant.getManager().getId(), response.getManagerId());
        assertEquals(restaurant.getCategories().stream().map(Category::getName).toList(), response.getCategories());
        assertEquals(restaurant.getWorkingHours(), response.getWorkingHours());
        assertEquals(restaurant.getAverageBill(), response.getAverageBill());
        assertEquals(AddressMapper.INSTANCE.toAddressResponse(restaurant.getAddress()), response.getAddress());
        assertEquals(restaurant.getPhoto().getId(), response.getPhoto());
        assertEquals(restaurant.getContract().getId(), response.getContract());
        assertEquals(restaurant.getActive(), response.getActive());
    }

    @Test
    public void testReturnRestaurantNotFound() {
        // arrange
        String code = "ABC123";
        when(restaurantRepository.findByRestaurantCode(code)).thenReturn(Optional.empty());

        // assert
        assertThrows(RestaurantNotFoundException.class, () -> {
            // act
            service.returnRestaurant(code);
        });
    }

    @Test
    @Transactional
    void testDisableRestaurantNotFound() {
        // arrange
        String restaurantCode = "ABCD123";
        when(restaurantRepository.findByRestaurantCode(restaurantCode)).thenReturn(Optional.empty());

        // assert
        assertThrows(RestaurantNotFoundException.class, () -> {
            // act
            service.disableRestaurant(restaurantCode);
        });
    }

    @Test
    @Transactional
    public void testDisableRestaurantSuccess() throws Exception {
        // arrange
        String code = "ABCDE";
        Restaurant restaurant = Restaurant.builder()
                .restaurantCode(code)
                .name("Test Restaurant")
                .description("Test description")
                .email("test@test.com")
                .phone("+37258535156")
                .workingHours("24/7")
                .averageBill(120)
                .active(true)
                .waiters(new ArrayList<>(Arrays.asList(
                        User.builder().email("waiter1@test.com").build(),
                        User.builder().email("waiter2@test.com").build(),
                        User.builder().email("waiter3@test.com").build()
                )))
                .manager(User.builder().email("manager@test.com").build())
                .build();

        when(restaurantRepository.findByRestaurantCode(code)).thenReturn(Optional.of(restaurant));

        // act
        RestaurantDisableResponse response = service.disableRestaurant(code);

        // assert
        assertNotNull(response);
        assertEquals("Restaurant was successfully disabled", response.getMessage());
    }

    @Test
    @Transactional
    public void testUpdateRestaurantSuccess() throws Exception {

        Address oldAddress = Address.builder()
                .country("Estonia")
                .city("Tallinn")
                .state("Harjumaa")
                .street("Zalupka 1/2")
                .zipCode("123456")
                .build();

        // Arrange
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantCode("ABCDEF");
        restaurant.setName("Test restaurant");
        restaurant.setDescription("Test restaurant description");
        restaurant.setEmail("testrestaurant@test.com");
        restaurant.setPhone("+37258535156");
        restaurant.setWorkingHours("24/7");
        restaurant.setAverageBill(100);
        restaurant.setAddress(oldAddress);
        restaurant.setManager(User.builder().email("testmanager@test.com").build());
        restaurant.setCategories(new ArrayList<>(Arrays.asList(Category.builder().name("Chinese").build(), Category.builder().name("Italian").build())));
        restaurant.setPhoto(File.builder().fileName("testphoto.jpg").build());
        restaurant.setContract(File.builder().fileName("testcontract.pdf").build());

        when(restaurantRepository.findByRestaurantCode(restaurant.getRestaurantCode())).thenReturn(Optional.of(restaurant));
        when(userRepository.findById(any())).thenReturn(Optional.of(restaurant.getManager()));

        AddressRequest newAddressRequest = AddressRequest.builder()
                .country("Estonia")
                .city("Tartu")
                .state("Tartumaa")
                .street("Kooli 5")
                .zipCode("654321")
                .build();

        Address newAddress = Address.builder()
                .country("Estonia")
                .city("Tartu")
                .state("Tartumaa")
                .street("Kooli 5")
                .zipCode("654321")
                .build();

        when(addressRepository.save(any())).thenReturn(newAddress);

        RestaurantUpdateRequest request = new RestaurantUpdateRequest();
        request.setRestaurantCode("ABCDEF");
        request.setRestaurantName("New Test restaurant name");
        request.setRestaurantDescription("New test restaurant description");
        request.setRestaurantEmail("newtestrestaurant@test.com");
        request.setRestaurantPhone("+3721234567");
        request.setWorkingHours("8am-9pm");
        request.setAverageBill(200);
        request.setAddress(newAddressRequest);
        request.setManagerId(restaurant.getManager().getId());
        request.setCategories(Arrays.asList("Chinese", "French"));
        request.setPhoto(new MockMultipartFile("testphoto.jpg", new byte[]{}));
        request.setContact(new MockMultipartFile("testcontract.pdf", new byte[]{}));

        // Act
        RestaurantUpdateResponse response = service.updateRestaurant(request);

        // Assert
        assertNotNull(response);
        assertEquals("Restaurant updated successfully", response.getMessage());

        Optional<Restaurant> optionalRestaurant = restaurantRepository.findByRestaurantCode(request.getRestaurantCode());
        assertTrue(optionalRestaurant.isPresent());
        Restaurant updatedRestaurant = optionalRestaurant.get();
        assertEquals(request.getRestaurantName(), updatedRestaurant.getName());
        assertEquals(request.getRestaurantDescription(), updatedRestaurant.getDescription());
        assertEquals(request.getRestaurantEmail(), updatedRestaurant.getEmail());
        assertEquals(request.getRestaurantPhone(), updatedRestaurant.getPhone());
        assertEquals(request.getWorkingHours(), updatedRestaurant.getWorkingHours());
        assertEquals(request.getAverageBill(), updatedRestaurant.getAverageBill());
    }

    @Test
    @Transactional
    public void testUpdateRestaurantFailedManagerNotFound() {
        // arrange
        RestaurantUpdateRequest request = new RestaurantUpdateRequest();
        request.setAddress(new AddressRequest());
        request.setManagerId(1L);
        when(restaurantRepository.findByRestaurantCode(any())).thenReturn(Optional.of(new Restaurant()));
        when(addressRepository.findByStreetAndCityAndApartmentNumberAndCountryAndStateAndZipCode(
                any(), any(), any(), any(), any(), any())
        ).thenReturn(Optional.of(new Address()));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // assert
        assertThrows(UsernameNotFoundException.class, () -> {
            // act
            service.updateRestaurant(request);
        });
    }

    @Test
    @Transactional
    public void testUpdateRestaurantRestaurantNotFound() {
        // arrange
        RestaurantUpdateRequest request = new RestaurantUpdateRequest();
        request.setRestaurantCode("invalidCode");

        when(restaurantRepository.findByRestaurantCode(request.getRestaurantCode())).thenReturn(Optional.empty());

        // assert
        assertThrows(RestaurantNotFoundException.class, () -> {
            // act
            service.updateRestaurant(request);
        });
    }
}