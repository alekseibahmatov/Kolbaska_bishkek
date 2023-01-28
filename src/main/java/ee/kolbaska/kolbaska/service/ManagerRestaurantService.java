package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.exception.RestaurantAlreadyExistsException;
import ee.kolbaska.kolbaska.exception.UserAlreadyExistsException;
import ee.kolbaska.kolbaska.model.category.Category;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.CategoryRepository;
import ee.kolbaska.kolbaska.repository.RestaurantRepository;
import ee.kolbaska.kolbaska.repository.RoleRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.RestaurantRequest;
import ee.kolbaska.kolbaska.request.WaiterRequest;
import ee.kolbaska.kolbaska.response.RestaurantResponse;
import ee.kolbaska.kolbaska.response.WaiterResponse;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.FormatService;
import ee.kolbaska.kolbaska.service.miscellaneous.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
}
