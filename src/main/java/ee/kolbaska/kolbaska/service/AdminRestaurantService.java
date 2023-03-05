package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.exception.RestaurantAlreadyExistsException;
import ee.kolbaska.kolbaska.exception.RestaurantNotFoundException;
import ee.kolbaska.kolbaska.exception.UserStillOnDutyException;
import ee.kolbaska.kolbaska.model.address.Address;
import ee.kolbaska.kolbaska.model.category.Category;
import ee.kolbaska.kolbaska.model.file.FileType;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.*;
import ee.kolbaska.kolbaska.request.RestaurantRequest;
import ee.kolbaska.kolbaska.response.RestaurantResponse;
import ee.kolbaska.kolbaska.response.RestaurantTableResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRestaurantService {

    private final UserRepository userRepository;

    private final RestaurantRepository restaurantRepository;

    private final CategoryRepository categoryRepository;

    private final StorageService storageService;

    private final AddressRepository addressRepository;

    private final EmailService emailService;
    private final RoleRepository roleRepository;

    @Transactional
    public RestaurantTableResponse createRestaurant(RestaurantRequest request) throws Exception {

        boolean restaurantExists = restaurantRepository.findByEmail(request.getRestaurantEmail()).isPresent();

        if (restaurantExists) throw new RestaurantAlreadyExistsException("Restaurant with following email is already exists, please check restaurant list");

        Address address = Address.builder()
                .street(request.getStreet())
                .state(request.getState())
                .zipCode(request.getPostalCode())
                .city(request.getCity())
                .country(request.getCountry())
                .build();

        address = addressRepository.save(address);

        Restaurant newRestaurant = Restaurant.builder()
                .name(request.getRestaurantName())
                .description(request.getRestaurantDescription())
                .workingHours(request.getWorkingHours())
                .averageBill(request.getAverageBill())
                .phone(request.getRestaurantPhone())
                .email(request.getRestaurantEmail())
                .restaurantCode(UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .categories(setupCategories(new HashSet<>(request.getCategories())))
                .photo(storageService.uploadFile(request.getPhoto(), FileType.PHOTO))
                .contract(storageService.uploadFile(request.getContract(), FileType.CONTRACT))
                .manager(getUser(request.getManagerEmail()))
                .address(address)
                .build();

        restaurantRepository.save(newRestaurant);

        return new RestaurantTableResponse(
                newRestaurant.getRestaurantCode(),
                newRestaurant.getName(),
                newRestaurant.getEmail(),
                newRestaurant.getPhone(),
                newRestaurant.getAverageBill()
        );
    }

    public List<RestaurantTableResponse> returnRestaurantList() {

        return restaurantRepository.findAll()
                .stream().map(restaurant -> new RestaurantTableResponse(
                        restaurant.getRestaurantCode(),
                        restaurant.getName(),
                        restaurant.getEmail(),
                        restaurant.getPhone(),
                        restaurant.getAverageBill()
                ))
                .collect(Collectors.toList());
    }

    public RestaurantResponse returnRestaurant(String code) throws Exception {
        Restaurant restaurant = restaurantRepository.findByRestaurantCode(code).orElseThrow(
                () -> new RestaurantNotFoundException("Restaurant with such code wasn't found")
        );

        List<String> categories = restaurant.getCategories().stream()
                .map(Category::getName).toList();

        return RestaurantResponse.builder()
                .restaurantName(restaurant.getName())
                .restaurantEmail(restaurant.getEmail())
                .restaurantPhone(restaurant.getPhone())
                .restaurantDescription(restaurant.getDescription())
                .managerId(restaurant.getManager().getId())
                .categories(categories)
                .workingHours(restaurant.getWorkingHours())
                .city(restaurant.getAddress().getCity())
                .country(restaurant.getAddress().getCountry())
                .province(restaurant.getAddress().getState())
                .postalCode(restaurant.getAddress().getZipCode())
                .photo(restaurant.getPhoto().getFileName())
                .contact(restaurant.getContract().getFileName())
                .build();
    }

    public List<String> getCategories() {
        return categoryRepository.findAll().stream()
                .map(Category::getName)
                .collect(Collectors.toList());
    }

    private User getUser(String email) throws UserStillOnDutyException {
        Optional<User> ifUser = userRepository.findByEmail(email);

        if (ifUser.isPresent()) {
            User user = ifUser.get();
            if(user.getRestaurant() != null) throw new UserStillOnDutyException("This user is already connected to restaurant, please ask him/her to leave or check whether email is correct");
            return user;
        }

        String activationCode = UUID.randomUUID().toString();

        User newUser = User.builder()
                .email(email)
                .activationCode(activationCode)
                .roles(roleRepository.findByRoleNameIn(List.of("ROLE_MANAGER", "ROLE_NEWBIE")))
                .activated(true)
                .deleted(false)
                .build();

        emailService.sendSimpleMessage(email, "Activate your account", String.format("Here is your uuid to activate you account: %s", activationCode));

        return userRepository.save(newUser);
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

    public Resource downloadFile(String fileName, String type) throws Exception {

        return storageService.getFile(fileName, type.equals("photo") ? FileType.PHOTO : FileType.CONTRACT);
    }
}
