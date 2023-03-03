package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.exception.RestaurantAlreadyExistsException;
import ee.kolbaska.kolbaska.model.address.Address;
import ee.kolbaska.kolbaska.model.category.Category;
import ee.kolbaska.kolbaska.model.file.File;
import ee.kolbaska.kolbaska.model.file.FileType;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.AddressRepository;
import ee.kolbaska.kolbaska.repository.CategoryRepository;
import ee.kolbaska.kolbaska.repository.RestaurantRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.RestaurantRequest;
import ee.kolbaska.kolbaska.response.RestaurantResponse;
import ee.kolbaska.kolbaska.response.RestaurantTableResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@TestPropertySource("/tests.properties")

class AdminRestaurantServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AdminRestaurantService adminRestaurantService;

    @Mock
    private StorageService storageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createRestaurant_validInput_returnsRestaurantCodeAndName() throws Exception {

        // Arrange
        RestaurantRequest request = new RestaurantRequest();
        request.setRestaurantName("Test Restaurant");
        request.setRestaurantDescription("Test Description");
        request.setWorkingHours("10am-10pm");
        request.setAverageBill(200);
        request.setRestaurantPhone("1234567890");
        request.setCity("Tallinn");
        request.setCountry("Estonia");
        request.setState("Harjumaa");
        request.setStreet("Joe 4c");
        request.setPostalCode("10159");
        request.setRestaurantEmail("test@test.com");
        request.setCategories(Arrays.asList("Test Category 1", "Test Category 2"));
        request.setManagerEmail("manager@test.com");
        request.setPhoto(new MockMultipartFile("photo.jpg", "photo.jpg", "image/jpeg", "photo".getBytes()));
        request.setContract(new MockMultipartFile("contact.pdf", "contact.pdf", "application/pdf", "contact".getBytes()));

        User user = new User();
        user.setId(1L);
        user.setFullName("Gavno");

        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByName("Test Category 1")).thenReturn(Optional.empty());
        when(categoryRepository.findByName("Test Category 2")).thenReturn(Optional.empty());

        // Act
        RestaurantTableResponse response = adminRestaurantService.createRestaurant(request);

        // Assert
        assertNotNull(response.getRestaurantCode());
        assertEquals("Test Restaurant", response.getRestaurantName());
        assertEquals("test@test.com", response.getRestaurantEmail());
        assertEquals("1234567890", response.getRestaurantPhone());
        assertEquals(200, response.getAverageBill(), 0);
    }

    @Test
    public void createRestaurant_existingEmail_throwsRestaurantAlreadyExistsException() throws Exception {
        // Arrange
        RestaurantRequest request = new RestaurantRequest();
        request.setRestaurantEmail("existing@test.com");
        when(restaurantRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(new Restaurant()));

        // Act and Assert
        assertThrows(RestaurantAlreadyExistsException.class, () -> adminRestaurantService.createRestaurant(request));
    }

    @Test
    public void createRestaurant_emptyCategories_throwsNullPointerException() throws Exception {
        // Arrange
        RestaurantRequest request = new RestaurantRequest();
        request.setCategories(new ArrayList<>());

        // Act and Assert
        assertThrows(NullPointerException.class, () -> adminRestaurantService.createRestaurant(request));
    }

    @Test
    void returnRestaurant_whenRestaurantWithCodeExists_thenReturnRestaurantResponse() throws Exception {
        Restaurant restaurant = new Restaurant();
        restaurant.setName("Test Restaurant");
        restaurant.setEmail("test@restaurant.com");
        restaurant.setPhone("1234567890");
        restaurant.setDescription("Test Description");
        User manager = new User();
        manager.setId(1L);
        restaurant.setManager(manager);
        Category category1 = new Category();
        category1.setName("Category 1");
        Category category2 = new Category();
        category2.setName("Category 2");
        restaurant.setCategories(Arrays.asList(category1, category2));
        restaurant.setWorkingHours("Test Working Hours");
        Address address = new Address();
        address.setCity("Test City");
        address.setCountry("Test Country");
        address.setState("Test State");
        address.setZipCode("Test ZipCode");
        restaurant.setAddress(address);
        File photo = new File();
        photo.setFileName("photo.jpeg");
        restaurant.setPhoto(photo);
        File contract = new File();
        contract.setFileName("contract.pdf");
        restaurant.setContract(contract);

        when(restaurantRepository.findByRestaurantCode(anyString()))
                .thenReturn(Optional.of(restaurant));

        when(storageService.getFile(anyString(), eq(FileType.PHOTO)))
                .thenReturn(new ByteArrayResource("photo".getBytes()).getByteArray());

        when(storageService.getFile(anyString(), eq(FileType.CONTRACT)))
                .thenReturn(new ByteArrayResource("contract".getBytes()).getByteArray());

        RestaurantResponse response = adminRestaurantService.returnRestaurant("test");

        assertThat(response.getRestaurantName()).isEqualTo("Test Restaurant");
        assertThat(response.getRestaurantEmail()).isEqualTo("test@restaurant.com");
        assertThat(response.getRestaurantPhone()).isEqualTo("1234567890");
        assertThat(response.getRestaurantDescription()).isEqualTo("Test Description");
        assertThat(response.getManagerId()).isEqualTo(1L);
        assertThat(response.getCategories()).containsExactlyInAnyOrder("Category 1", "Category 2");
        assertThat(response.getWorkingHours()).isEqualTo("Test Working Hours");
        assertThat(response.getCity()).isEqualTo("Test City");
        assertThat(response.getCountry()).isEqualTo("Test Country");
        assertThat(response.getProvince()).isEqualTo("Test State");
        assertThat(response.getPostalCode()).isEqualTo("Test ZipCode");
        assertThat(response.getPhoto()).isEqualTo("photo".getBytes());
        assertThat(response.getContact()).isEqualTo("contract".getBytes());
    }
}