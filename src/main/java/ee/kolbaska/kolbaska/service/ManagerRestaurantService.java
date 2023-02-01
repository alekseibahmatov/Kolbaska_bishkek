package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.config.RequestContextFilter;
import ee.kolbaska.kolbaska.exception.RestaurantAlreadyExistsException;
import ee.kolbaska.kolbaska.exception.RestaurantNotFoundException;
import ee.kolbaska.kolbaska.exception.UserAlreadyExistsException;
import ee.kolbaska.kolbaska.model.category.Category;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.transaction.Transaction;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.CategoryRepository;
import ee.kolbaska.kolbaska.repository.RestaurantRepository;
import ee.kolbaska.kolbaska.repository.RoleRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.RestaurantRequest;
import ee.kolbaska.kolbaska.request.WaiterRequest;
import ee.kolbaska.kolbaska.response.RestaurantResponse;
import ee.kolbaska.kolbaska.response.WaiterDeletedResponse;
import ee.kolbaska.kolbaska.response.WaiterResponse;
import ee.kolbaska.kolbaska.security.JwtService;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.FormatService;
import ee.kolbaska.kolbaska.service.miscellaneous.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.management.relation.RoleNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

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

    public ResponseEntity<WaiterResponse> createWaiter(WaiterRequest request) throws Exception {

        boolean userExists = userRepository.findByEmail(request.getEmail()).isPresent();

        if (userExists) throw new UserAlreadyExistsException("User already exists");

        String phoneFormatted = formatService.formatE164(request.getPhone());

        String securePassword = passwordService.generatePassword(10);

        emailService.sendSimpleMessage(request.getEmail(), "Password", String.format("Here is your password for accessing qr code page: %s", securePassword));

        Restaurant restaurant = restaurantRepository.findByRestaurantCode(request.getRestaurantCode()).orElseThrow(() -> new NoSuchElementException("There is no restaurant with code: " + request.getRestaurantCode()));

        User waiter = User.builder()
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

        WaiterResponse response = WaiterResponse.builder()
                .id(waiter.getId())
                .phone(waiter.getPhone())
                .turnover(0.0)
                .email(waiter.getEmail())
                .fullName(waiter.getFullName())
                .build();

        return ResponseEntity.ok(response);

    }

    public ResponseEntity<WaiterDeletedResponse> deleteWaiter(Long id) {
        Optional<User> waiterExists = userRepository.findById(id);

        if (waiterExists.isEmpty()) throw new UsernameNotFoundException(String.format("User with id: %x not found", id));

        User waiter = waiterExists.get();

        waiter.setDeleted(true);
        waiter.setDeletedAt(new Date());

        userRepository.save(waiter);

        WaiterDeletedResponse response = WaiterDeletedResponse.builder()
                .id(id)
                .deleted(true)
                .build();

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<List<WaiterResponse>> getWaiters() throws RestaurantNotFoundException {
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

        return ResponseEntity.ok(response);
    }
}
