package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.config.UserConfiguration;
import ee.kolbaska.kolbaska.exception.RestaurantNotFoundException;
import ee.kolbaska.kolbaska.exception.UserStillOnDutyException;
import ee.kolbaska.kolbaska.mapper.AddressMapper;
import ee.kolbaska.kolbaska.mapper.LoginMapper;
import ee.kolbaska.kolbaska.mapper.TransactionMapper;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.transaction.Transaction;
import ee.kolbaska.kolbaska.model.user.Role;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.AddressRepository;
import ee.kolbaska.kolbaska.repository.RestaurantRepository;
import ee.kolbaska.kolbaska.repository.RoleRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.ManagerCustomerUpdateRequest;
import ee.kolbaska.kolbaska.request.WaiterRequest;
import ee.kolbaska.kolbaska.response.*;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.FormatService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.RoleNotFoundException;
import java.util.*;

import static ee.kolbaska.kolbaska.service.AdminWaiterService.updateWaiterImpl;

@Service
@RequiredArgsConstructor
public class ManagerRestaurantService {

    private final UserRepository userRepository;

    private final EmailService emailService;

    private final RoleRepository roleRepository;

    private final RestaurantRepository restaurantRepository;

    private final UserConfiguration userConfiguration;

    private final PasswordEncoder passwordEncoder;

    private final FormatService formatService;

    private final AddressRepository addressRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagerRestaurantService.class);

    @Transactional
    public WaiterResponse createWaiter(WaiterRequest request) throws Exception {
        LOGGER.info("Creating waiter with following request: {}", request);

        User manager = userConfiguration.getRequestUser();

        if (manager.getManagedRestaurant() == null) {
            LOGGER.info("This manager ({}) is not assigned to any restaurant", manager.getEmail());
            throw new RestaurantNotFoundException("This manager is not assigned to any restaurant");
        }

        Optional<User> userExists = userRepository.findByEmail(request.getEmail());
        Restaurant restaurant = manager.getManagedRestaurant();
        User waiter;

        if (userExists.isEmpty()) {
            String activationCode = UUID.randomUUID().toString();
            Role waiterRole = roleRepository.findRoleByRoleName("ROLE_WAITER").orElseThrow(RoleNotFoundException::new);
            Role newbieRole = roleRepository.findRoleByRoleName("ROLE_NEWBIE").orElseThrow(RoleNotFoundException::new);

            waiter = User.builder()
                    .email(request.getEmail())
                    .roles(List.of(waiterRole, newbieRole))
                    .activationCode(activationCode)
                    .activated(true)
                    .restaurant(restaurant)
                    .build();

            emailService.sendSimpleMessage(
                    request.getEmail(),
                    "Activate your account",
                    String.format("Here is your uuid to activate your account: %s", activationCode)
            );

            waiter = userRepository.save(waiter);

            LOGGER.info("New waiter created with email {}", waiter.getEmail());
        } else {
            waiter = userExists.get();

            if (waiter.getRestaurant() != null) {
                LOGGER.info("User with email {} is currently connected to a restaurant. Please ask them to disconnect from their previous restaurant.", waiter.getEmail());
                throw new UserStillOnDutyException("User is currently connected to a restaurant. Please ask them to disconnect from their previous restaurant.");
            }

            waiter.setRestaurant(restaurant);
        }

        if (restaurant.getWaiters() == null) {
            restaurant.setWaiters(List.of(waiter));
        } else {
            restaurant.getWaiters().add(waiter);
        }

        restaurantRepository.save(restaurant);

        LOGGER.info("New waiter with email {} added to restaurant with id {}", waiter.getEmail(), restaurant.getId());

        return WaiterResponse.builder()
                .id(waiter.getId())
                .phone(waiter.getPhone())
                .turnover(0.0)
                .email(waiter.getEmail())
                .fullName(waiter.getFullName())
                .build();
    }

    public WaiterDeletedResponse deleteWaiter(Long id) {
        LOGGER.info("Deleting waiter with id {}", id);

        Optional<User> waiterExists = userRepository.findById(id);

        if (waiterExists.isEmpty()) {
            LOGGER.info("Waiter with id {} not found", id);
            throw new UsernameNotFoundException("Waiter not found!");
        }

        User waiter = waiterExists.get();

        User manager = userConfiguration.getRequestUser();

        Restaurant restaurant = manager.getManagedRestaurant();

        if (restaurant.getWaiters() == null || !restaurant.getWaiters().contains(waiter)) {
            LOGGER.info("Waiter with email {} not found in restaurant with id {}", waiter.getEmail(), restaurant.getId());
            throw new UsernameNotFoundException("Waiter not found!");
        }

        waiter.setRestaurant(null);

        userRepository.save(waiter);

        LOGGER.info("Waiter with email {} deleted from restaurant with id {}", waiter.getEmail(), restaurant.getId());

        return WaiterDeletedResponse.builder()
                .id(id)
                .deleted(true)
                .build();
    }

    public List<WaiterResponse> getWaiters() throws RestaurantNotFoundException {
        User manager = userConfiguration.getRequestUser();

        LOGGER.info("Returning list of waiters for this manager: {}", manager);

        Restaurant restaurant = manager.getManagedRestaurant();

        if (restaurant == null) {
            LOGGER.error("No restaurant found for manager with id {}", manager.getId());
            throw new RestaurantNotFoundException("This manager is not associated with any restaurant!");
        }

        List<User> waiters = restaurant.getWaiters();

        List<WaiterResponse> response = new ArrayList<>();

        for (User user : waiters) {
            double turnover;

            if (user.getTransactions() != null) {
                turnover = user.getTransactions().stream()
                        .mapToDouble(Transaction::getValue)
                        .sum();
            } else turnover = 0.0;

            WaiterResponse waiterResponse = WaiterResponse.builder()
                    .id(user.getId())
                    .fullName(user.getFullName())
                    .phone(user.getPhone())
                    .email(user.getEmail())
                    .turnover(turnover)
                    .build();

            response.add(waiterResponse);
        }

        LOGGER.info("Retrieved waiters for restaurant with id {}", restaurant.getId());

        return response;
    }

    public CustomerInformationResponse getWaiter(Long id) {
        User manager = userConfiguration.getRequestUser();

        LOGGER.info("Retrieving waiter's data for manager: {}", manager);

        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            LOGGER.info("Waiter with id {} was not found", id);
            throw new UsernameNotFoundException("Waiter with such id was not found");
        }

        User user = optionalUser.get();
        Restaurant restaurant = manager.getManagedRestaurant();

        if (restaurant.getWaiters() == null || !restaurant.getWaiters().contains(user)) {
            LOGGER.info("Waiter with id {} was not found in the managed restaurant", id);
            throw new UsernameNotFoundException("Waiter that you requested does not exist");
        }

        AddressResponse addressResponse = AddressMapper.INSTANCE.toAddressResponse(user.getAddress());
        List<LoginResponse> loginResponses = LoginMapper.INSTANCE.toLoginResponse(user.getLogins().stream());

        CustomerInformationResponse.CustomerInformationResponseBuilder response = CustomerInformationResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(addressResponse)
                .personalCode(user.getPersonalCode())
                .transactions(TransactionMapper.INSTANCE.toTransactionResponseList(user.getTransactions()))
                .logins(loginResponses);

        LOGGER.info("Waiter with id {} was successfully retrieved", id);

        return response.build();
    }

    public CustomerUpdateResponse updateWaiter(ManagerCustomerUpdateRequest request) throws UsernameNotFoundException {
        User manager = userConfiguration.getRequestUser();

        LOGGER.info("Updating waiter's data for manager: {}, with request: {}", manager, request);

        Optional<User> ifUser = userRepository.findById(request.getId());

        if (ifUser.isEmpty()) {
            LOGGER.error("Waiter with id {} not found.", request.getId());
            throw new UsernameNotFoundException("Waiter with such id wasn't found");
        }

        Restaurant restaurant = manager.getManagedRestaurant();

        User user = ifUser.get();

        if (!restaurant.getWaiters().contains(user)) {
            LOGGER.error("Waiter with id {} is not associated with the current manager's restaurant.", request.getId());
            throw new UsernameNotFoundException("Waiter that you request does nopt exists");
        }

        updateWaiterImpl(user, request.getFullName(), request.getNewPassword(), passwordEncoder, formatService, request.getPhone(), request.getPersonalCode(), request.getAddress(), addressRepository, request.getEmail());

        userRepository.save(user);

        LOGGER.info("Waiter with id {} successfully updated.", request.getId());

        return CustomerUpdateResponse.builder()
                .message("User was successfully updated!")
                .build();
    }
}
