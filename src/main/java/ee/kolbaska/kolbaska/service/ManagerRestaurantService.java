package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.exception.RestaurantNotFoundException;
import ee.kolbaska.kolbaska.exception.UserStillOnDutyExceptions;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.transaction.Transaction;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.RestaurantRepository;
import ee.kolbaska.kolbaska.repository.RoleRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.WaiterRequest;
import ee.kolbaska.kolbaska.response.WaiterDeletedResponse;
import ee.kolbaska.kolbaska.response.WaiterResponse;
import ee.kolbaska.kolbaska.security.JwtService;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.FormatService;
import ee.kolbaska.kolbaska.service.miscellaneous.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.management.relation.RoleNotFoundException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ManagerRestaurantService {

    private final UserRepository userRepository;

    private final FormatService formatService;

    private final PasswordService passwordService;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    private final RestaurantRepository restaurantRepository;

    private final JwtService jwtService;

    public WaiterResponse createWaiter(WaiterRequest request) throws Exception {

        Optional<User> userExists = userRepository.findByPersonalCode(request.getPersonalCode());

        Restaurant restaurant = restaurantRepository.findByRestaurantCode(request.getRestaurantCode()).orElseThrow(() -> new RestaurantNotFoundException("There is no restaurant with code: " + request.getRestaurantCode()));

        User waiter;

        if (userExists.isEmpty()) {
            String phoneFormatted = formatService.formatE164(request.getPhone());

            String securePassword = passwordService.generatePassword(10);

            emailService.sendSimpleMessage(request.getEmail(), "Password", String.format("Here is your password for accessing qr code page: %s", securePassword));

            waiter = User.builder()
                    .fullName(request.getFullName())
                    .phone(phoneFormatted)
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(securePassword))
                    .role(roleRepository.findRoleByRoleName("ROLE_WAITER").orElseThrow(RoleNotFoundException::new))
                    .activated(true)
                    .deleted(false)
                    .restaurant(restaurant)
                    .build();

            waiter = userRepository.save(waiter);

        } else {
            waiter = userExists.get();

            if (waiter.getRestaurant() != null) throw new UserStillOnDutyExceptions("User is currently connected to restaurant. Please ask him to disconnect from previous restaurant");

            waiter.setRestaurant(restaurant);
        }

        if(restaurant.getWaiters() == null) restaurant.setWaiters(List.of(waiter));
        else restaurant.getWaiters().add(waiter);

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

        if (waiterExists.isEmpty()) throw new UsernameNotFoundException(String.format("User with id: %x not found", id));

        User waiter = waiterExists.get();

        waiter.setDeleted(true);
        waiter.setDeletedAt(new Date());

        Restaurant restaurant = waiter.getRestaurant();
        restaurant.setWaiters(restaurant.getWaiters().stream().filter((rest) -> !Objects.equals(rest.getId(), waiter.getId())).toList());

        waiter.setRestaurant(null);

        userRepository.save(waiter);
        restaurantRepository.save(restaurant);

        return WaiterDeletedResponse.builder()
                .id(id)
                .deleted(true)
                .build();
    }

    public List<WaiterResponse> getWaiters() throws RestaurantNotFoundException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        final String jwtToken = request.getHeader("Authorization").substring(7);

        final String email = jwtService.extractUserEmail(jwtToken);

        User manager = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User with such email not found!")
        );

        Restaurant restaurant = manager.getRestaurant();

        if (restaurant == null) throw new RestaurantNotFoundException("This manager is not associated with any restaurant!");

        List<User> usersWithoutManager = restaurant.getWaiters().stream()
                .filter(user -> !Objects.equals(user.getEmail(), email)).toList();

        List<WaiterResponse> response = new ArrayList<>();

        for (User u: usersWithoutManager) {
            double turnover = 0.0;

            for (Transaction t: u.getTransactions()) {
                turnover += t.getValue();
            }

            WaiterResponse waiterResponse = WaiterResponse.builder()
                    .id(u.getId())
                    .fullName(u.getFullName())
                    .phone(u.getPhone())
                    .email(u.getEmail())
                    .turnover(turnover)
                    .build();

            response.add(waiterResponse);
        }

        return response;
    }
}
