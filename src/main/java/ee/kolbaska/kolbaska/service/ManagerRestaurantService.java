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

    @Transactional
    public WaiterResponse createWaiter(WaiterRequest request) throws Exception {
        User manager = userConfiguration.getRequestUser();

        if (manager.getManagedRestaurant() == null) {
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
                    .deleted(false)
                    .restaurant(restaurant)
                    .build();

            emailService.sendSimpleMessage(
                    request.getEmail(),
                    "Activate your account",
                    String.format("Here is your uuid to activate your account: %s", activationCode)
            );

            waiter = userRepository.save(waiter);

        } else {
            waiter = userExists.get();

            if (waiter.getRestaurant() != null) {
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

        return WaiterResponse.builder()
                .id(waiter.getId())
                .phone(waiter.getPhone())
                .turnover(0.0)
                .email(waiter.getEmail())
                .fullName(waiter.getFullName())
                .build();
    }

    public WaiterDeletedResponse deleteWaiter(Long id) {
        Optional<User> waiterExists = userRepository.findById(id);

        if (waiterExists.isEmpty()) throw new UsernameNotFoundException("Waiter not found!");

        User waiter = waiterExists.get();

        User manager = userConfiguration.getRequestUser();

        Restaurant restaurant = manager.getManagedRestaurant();

        if (restaurant.getWaiters() == null || !restaurant.getWaiters().contains(waiter)) throw new UsernameNotFoundException("Waiter not found!");

        waiter.setRestaurant(null);

        userRepository.save(waiter);

        return WaiterDeletedResponse.builder()
                .id(id)
                .deleted(true)
                .build();
    }

    public List<WaiterResponse> getWaiters() throws RestaurantNotFoundException {

        User manager = userConfiguration.getRequestUser();

        Restaurant restaurant = manager.getManagedRestaurant();

        if (restaurant == null) throw new RestaurantNotFoundException("This manager is not associated with any restaurant!");

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

        return response;
    }

    public CustomerInformationResponse getWaiter(Long id) {
        User manager = userConfiguration.getRequestUser();
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("Waiter with such id was not found");
        }

        User user = optionalUser.get();
        Restaurant restaurant = manager.getManagedRestaurant();

        if (restaurant.getWaiters() == null || !restaurant.getWaiters().contains(user)) {
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

        return response.build();
    }

    public CustomerUpdateResponse updateWaiter(ManagerCustomerUpdateRequest request) throws UsernameNotFoundException {
        User manager = userConfiguration.getRequestUser();

        Optional<User> ifUser = userRepository.findById(request.getId());

        if (ifUser.isEmpty()) throw new UsernameNotFoundException("Waiter with such id wasn't found");

        Restaurant restaurant = manager.getManagedRestaurant();

        User user = ifUser.get();

        if (!restaurant.getWaiters().contains(user)) throw new UsernameNotFoundException("Waiter that you request does nopt exists");

        updateWaiterImpl(user, request.getFullName(), request.getNewPassword(), passwordEncoder, formatService, request.getPhone(), request.getPersonalCode(), request.getAddress(), addressRepository, request.getEmail());

        userRepository.save(user);

        return CustomerUpdateResponse.builder()
                .message("User was successfully updated!")
                .build();
    }
}
