package ee.kolbaska.kolbaska.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import ee.kolbaska.kolbaska.model.address.Address;
import ee.kolbaska.kolbaska.model.category.Category;
import ee.kolbaska.kolbaska.model.file.File;
import ee.kolbaska.kolbaska.model.file.FileType;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.RestaurantRepository;
import ee.kolbaska.kolbaska.response.RestaurantResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.TestPropertySource;

@ExtendWith(MockitoExtension.class)
@TestPropertySource("/tests.properties")
class AdminServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private AdminService adminService;

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

        RestaurantResponse response = adminService.returnRestaurant("test");

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
