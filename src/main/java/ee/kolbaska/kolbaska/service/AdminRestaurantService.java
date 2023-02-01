package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.exception.RestaurantAlreadyExistsException;
import ee.kolbaska.kolbaska.model.category.Category;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.CategoryRepository;
import ee.kolbaska.kolbaska.repository.RestaurantRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.RestaurantRequest;
import ee.kolbaska.kolbaska.response.RestaurantResponse;
import ee.kolbaska.kolbaska.response.RestaurantTableResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRestaurantService {

    private final UserRepository userRepository;

    private final RestaurantRepository restaurantRepository;

    private final CategoryRepository categoryRepository;

    private final FileService fileService;

    public RestaurantTableResponse createRestaurant(RestaurantRequest request) throws Exception {

        boolean restaurantExists = restaurantRepository.findByEmail(request.getRestaurantEmail()).isPresent();

        if (restaurantExists) throw new RestaurantAlreadyExistsException("Restaurant with following email is already exists, please check restaurant list");

        Restaurant newRestaurant = Restaurant.builder()
                .name(request.getRestaurantName())
                .description(request.getRestaurantDescription())
                .workingHours(request.getWorkingHours())
                .averageBill(request.getAverageBill())
                .phone(request.getRestaurantPhone())
                .email(request.getRestaurantEmail())
                .restaurantCode(UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .categories(setupCategories(new HashSet<>(request.getCategories())))
                .photoName(fileService.storeFile(request.getPhoto()))
                .contractName(fileService.storeFile(request.getContact()))
                .waiters(List.of(getUser(request.getManagerEmail())))
                .build();

        return new RestaurantTableResponse(
                newRestaurant.getRestaurantCode(),
                newRestaurant.getName(),
                newRestaurant.getEmail(),
                newRestaurant.getPhone(),
                newRestaurant.getAverageBill()
        );
    }

    private User getUser(String email) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) return user.get();

        User newUser = User.builder()
                .email(email)
                .activationCode(UUID.randomUUID().toString())
                .activated(true)
                .build();

        return userRepository.save(newUser);
    }

    public List<String> getCategories() {
        return categoryRepository.findAll().stream()
                .map(Category::getName)
                .collect(Collectors.toList());
    }

    private List<Category> setupCategories(Set<String> categories) {

        if (categories.isEmpty()) throw new NullPointerException("Categories are empty");

        List<Category> categoryList = new ArrayList<>();

        for (String categoryName : categories) {
            Optional<Category> category = categoryRepository.findByName(categoryName);

            if(category.isEmpty()) {
                Category newCategory = Category.builder()
                        .name(categoryName)
                        .build();

                newCategory = categoryRepository.save(newCategory);

                categoryList.add(newCategory);

                continue;
            }

            categoryList.add(category.get());
        }
        return categoryList;
    }
}
