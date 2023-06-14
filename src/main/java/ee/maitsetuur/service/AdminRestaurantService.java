package ee.maitsetuur.service;

import ee.maitsetuur.exception.RestaurantAlreadyExistsException;
import ee.maitsetuur.exception.RestaurantNotFoundException;
import ee.maitsetuur.exception.UserStillOnDutyException;
import ee.maitsetuur.mapper.AddressMapper;
import ee.maitsetuur.model.address.Address;
import ee.maitsetuur.model.category.Category;
import ee.maitsetuur.model.file.FileType;
import ee.maitsetuur.model.restaurant.Restaurant;
import ee.maitsetuur.model.user.User;
import ee.maitsetuur.repository.*;
import ee.maitsetuur.request.RestaurantRequest;
import ee.maitsetuur.request.RestaurantUpdateRequest;
import ee.maitsetuur.response.RestaurantDisableResponse;
import ee.maitsetuur.response.RestaurantResponse;
import ee.maitsetuur.response.RestaurantTableResponse;
import ee.maitsetuur.response.RestaurantUpdateResponse;
import ee.maitsetuur.service.miscellaneous.EmailService;
import ee.maitsetuur.service.miscellaneous.StorageService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${front.baseurl}")
    private String FRONT_BASEURL;

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
                .maitsetuurShare(request.getMaitsetuurShare())
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
                .maitsetuurShare(restaurant.getMaitsetuurShare())
                .categories(categories)
                .workingHours(restaurant.getWorkingHours())
                .averageBill(restaurant.getAverageBill())
                .address(AddressMapper.INSTANCE.toAddressResponse(restaurant.getAddress()))
                .photo(restaurant.getPhoto().getId().toString())
                .contract(restaurant.getContract().getId().toString())
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

    private User getUser(String email) throws UserStillOnDutyException, MessagingException {
        LOGGER.info("Getting user with email {}", email);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            if (user.getManagedRestaurant() != null) {
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
                .build();

        Map<String, Object> content = new HashMap<>();
        content.put("url", "%s/add-personal-info/%s".formatted(FRONT_BASEURL, activationCode));

        emailService.sendHTMLEmail(email, "Activate your account", "completeRegistration", content);

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
        restaurant.setDeleted(true);
        restaurant.getWaiters().forEach(waiter -> {
            LOGGER.info("Disabling user with email {}", waiter.getEmail());
            waiter.setRestaurant(null);
            waiter.setDeleted(true);
        });

        userRepository.saveAll(restaurant.getWaiters());
        restaurantRepository.save(restaurant);

        User manager = restaurant.getManager();
        manager.setDeleted(true);

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
                request.getAddress().getStreet(),
                request.getAddress().getCity(),
                request.getAddress().getApartmentNumber(),
                request.getAddress().getCountry(),
                request.getAddress().getState(),
                request.getAddress().getZipCode()
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
        restaurant.setMaitsetuurShare(restaurant.getMaitsetuurShare());
        restaurant.setWorkingHours(request.getWorkingHours());
        restaurant.setAverageBill(request.getAverageBill());
        restaurant.setCategories(setupCategories(new HashSet<>(request.getCategories())));
        restaurant.setPhoto(storageService.uploadFile(request.getPhoto(), FileType.PHOTO));
        restaurant.setContract(storageService.uploadFile(request.getContract(), FileType.CONTRACT));

        restaurantRepository.save(restaurant);

        RestaurantUpdateResponse response = RestaurantUpdateResponse.builder()
                .message("Restaurant updated successfully")
                .build();

        LOGGER.info("Restaurant updated: {}", response);

        return response;
    }
}
