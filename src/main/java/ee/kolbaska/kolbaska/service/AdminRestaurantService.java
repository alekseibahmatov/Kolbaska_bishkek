package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.exception.RestaurantAlreadyExistsException;
import ee.kolbaska.kolbaska.exception.RestaurantNotFoundException;
import ee.kolbaska.kolbaska.exception.UserStillOnDutyException;
import ee.kolbaska.kolbaska.mapper.AddressMapper;
import ee.kolbaska.kolbaska.model.address.Address;
import ee.kolbaska.kolbaska.model.category.Category;
import ee.kolbaska.kolbaska.model.file.FileType;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.*;
import ee.kolbaska.kolbaska.request.RestaurantRequest;
import ee.kolbaska.kolbaska.request.RestaurantUpdateRequest;
import ee.kolbaska.kolbaska.response.RestaurantDisableResponse;
import ee.kolbaska.kolbaska.response.RestaurantResponse;
import ee.kolbaska.kolbaska.response.RestaurantTableResponse;
import ee.kolbaska.kolbaska.response.RestaurantUpdateResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.StorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminRestaurantService.class);

    @Transactional
    public RestaurantTableResponse createRestaurant(RestaurantRequest request) throws Exception {
        LOGGER.info("Creating restaurant with request {}", request);

        boolean restaurantExists = restaurantRepository.findByEmail(request.getRestaurantEmail()).isPresent();
        if (restaurantExists) {
            throw new RestaurantAlreadyExistsException("Restaurant with following email is already exists, please check restaurant list");
        }

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
                .active(true)
                .build();

        newRestaurant = restaurantRepository.save(newRestaurant);

        RestaurantTableResponse response = new RestaurantTableResponse(
                newRestaurant.getRestaurantCode(),
                newRestaurant.getName(),
                newRestaurant.getEmail(),
                newRestaurant.getPhone(),
                newRestaurant.getAverageBill()
        );

        LOGGER.info("Restaurant created: {}", response);

        return response;
    }

    public List<RestaurantTableResponse> returnRestaurantList() {
        LOGGER.info("Returning restaurant list");

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
        LOGGER.info("Returning restaurant with code {}", code);

        Restaurant restaurant = restaurantRepository.findByRestaurantCode(code).orElseThrow(
                () -> new RestaurantNotFoundException("Restaurant with such code wasn't found")
        );

        List<String> categories = restaurant.getCategories().stream()
                .map(Category::getName).toList();

        RestaurantResponse response = RestaurantResponse.builder()
                .restaurantName(restaurant.getName())
                .restaurantEmail(restaurant.getEmail())
                .restaurantPhone(restaurant.getPhone())
                .restaurantDescription(restaurant.getDescription())
                .managerId(restaurant.getManager().getId())
                .categories(categories)
                .workingHours(restaurant.getWorkingHours())
                .averageBill(restaurant.getAverageBill())
                .address(AddressMapper.INSTANCE.toAddressResponse(restaurant.getAddress()))
                .photo(restaurant.getPhoto().getId())
                .contract(restaurant.getContract().getId())
                .active(restaurant.getActive())
                .build();

        LOGGER.info("Returned restaurant: {}", response);

        return response;
    }

    public List<String> getCategories() {
        LOGGER.info("Returning categories");

        return categoryRepository.findAll().stream()
                .map(Category::getName)
                .collect(Collectors.toList());
    }

    private User getUser(String email) throws UserStillOnDutyException {
        LOGGER.info("Getting user with email {}", email);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            if (user.getRestaurant() != null) {
                throw new UserStillOnDutyException("This user is already connected to a restaurant, please ask them to leave or check if the email is correct.");
            }

            LOGGER.info("Got user: {}", user);

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

        LOGGER.info("Created new user: {}", newUser);

        return userRepository.save(newUser);
    }

    private List<Category> setupCategories(Set<String> categories) {
        LOGGER.info("Setting up categories with set {}", categories);

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
        LOGGER.info("Categories set up: {}", categoryList);

        return categoryList;
    }

    @Transactional
    public RestaurantDisableResponse disableRestaurant(String code) throws RestaurantNotFoundException {
        LOGGER.info("Disabling restaurant with code {}", code);

        Restaurant restaurant = restaurantRepository.findByRestaurantCode(code).orElseThrow(
                () -> new RestaurantNotFoundException("Restaurant with given code do not found!")
        );

        restaurant.setActive(false);
        restaurant.setDeletedAt(new Date());
        restaurant.getWaiters().forEach(waiter -> {
            LOGGER.info("Disabling user with email {}", waiter.getEmail());
            waiter.setRestaurant(null);
            waiter.setDeleted(true);
            waiter.setDeletedAt(new Date());
        });

        userRepository.saveAll(restaurant.getWaiters());
        restaurantRepository.save(restaurant);

        User manager = restaurant.getManager();
        manager.setDeleted(true);
        manager.setDeletedAt(new Date());

        userRepository.save(manager);

        RestaurantDisableResponse response = RestaurantDisableResponse.builder()
                .message("Restaurant was successfully disabled")
                .build();

        LOGGER.info("Restaurant disabled: {}", response);

        return response;
    }

    public RestaurantUpdateResponse updateRestaurant(RestaurantUpdateRequest request) throws Exception {
        LOGGER.info("Updating restaurant with request {}", request);

        Restaurant restaurant = restaurantRepository.findByRestaurantCode(request.getRestaurantCode()).orElseThrow(
                () -> new RestaurantNotFoundException("Restaurant with given code not found!")
        );

        Optional<Address> ifCurrentAddress = addressRepository.findByStreetAndCityAndApartmentNumberAndCountryAndStateAndZipCode(
                restaurant.getAddress().getStreet(),
                restaurant.getAddress().getCity(),
                restaurant.getAddress().getApartmentNumber(),
                restaurant.getAddress().getCountry(),
                restaurant.getAddress().getState(),
                restaurant.getAddress().getZipCode()
        );

        Address currentAddress;

        currentAddress = ifCurrentAddress.orElseGet(() -> addressRepository.save(AddressMapper.INSTANCE.toAddress(request.getAddress())));

        restaurant.setAddress(currentAddress);
        restaurant.setName(request.getRestaurantName());
        restaurant.setDescription(request.getRestaurantDescription());
        restaurant.setEmail(request.getRestaurantEmail());
        restaurant.setPhone(request.getRestaurantPhone());

        User manager = userRepository.findById(request.getManagerId()).orElseThrow(
                () -> new UsernameNotFoundException("Manager not found!")
        );

        restaurant.setManager(manager);
        restaurant.setWorkingHours(request.getWorkingHours());
        restaurant.setAverageBill(request.getAverageBill());
        restaurant.setCategories(setupCategories(new HashSet<>(request.getCategories())));
        restaurant.setPhoto(storageService.uploadFile(request.getPhoto(), FileType.PHOTO));
        restaurant.setContract(storageService.uploadFile(request.getContact(), FileType.CONTRACT));

        restaurantRepository.save(restaurant);

        RestaurantUpdateResponse response = RestaurantUpdateResponse.builder()
                .message("Restaurant updated successfully")
                .build();

        LOGGER.info("Restaurant updated: {}", response);

        return response;
    }
}
