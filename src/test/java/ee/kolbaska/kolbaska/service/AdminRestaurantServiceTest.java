package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.exception.RestaurantAlreadyExistsException;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.CategoryRepository;
import ee.kolbaska.kolbaska.repository.RestaurantRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.RestaurantRequest;
import ee.kolbaska.kolbaska.response.RestaurantTableResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AdminRestaurantServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private AdminRestaurantService adminRestaurantService;

    @Mock
    private FileService fileService;

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
        request.setRestaurantEmail("test@test.com");
        request.setCategories(Arrays.asList("Test Category 1", "Test Category 2"));
        request.setManagerEmail("manager@test.com");
        request.setPhoto(new MockMultipartFile("photo.jpg", "photo.jpg", "image/jpeg", "photo".getBytes()));
        request.setContact(new MockMultipartFile("contact.pdf", "contact.pdf", "application/pdf", "contact".getBytes()));

        User user = new User();
        user.setId(1L);
        user.setFullName("Gavno");

        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findByName("Test Category 1")).thenReturn(Optional.empty());
        when(categoryRepository.findByName("Test Category 2")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<RestaurantTableResponse> response = adminRestaurantService.createRestaurant(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        RestaurantTableResponse content = response.getBody();
        assertNotNull(content.getRestaurantCode());
        assertEquals("Test Restaurant", content.getRestaurantName());
        assertEquals("test@test.com", content.getRestaurantEmail());
        assertEquals("1234567890", content.getRestaurantPhone());
        assertEquals(200, content.getAverageBill(), 0);
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
}