package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.exception.UserAlreadyExistsException;
import ee.kolbaska.kolbaska.mapper.AddressMapper;
import ee.kolbaska.kolbaska.model.address.Address;
import ee.kolbaska.kolbaska.model.user.Role;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.AddressRepository;
import ee.kolbaska.kolbaska.repository.RoleRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.*;
import ee.kolbaska.kolbaska.response.ActivationCodeValidationResponse;
import ee.kolbaska.kolbaska.response.AuthenticationResponse;
import ee.kolbaska.kolbaska.response.PersonalDataResponse;
import ee.kolbaska.kolbaska.response.RecoveryResponse;
import ee.kolbaska.kolbaska.security.JwtService;
import ee.kolbaska.kolbaska.service.miscellaneous.EmailService;
import ee.kolbaska.kolbaska.service.miscellaneous.FormatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.RoleNotFoundException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    private final EmailService emailService;

    private final FormatService formatService;

    private final AddressRepository addressRepository;


    public AuthenticationResponse register(RegisterRequest request) throws Exception {

        boolean userExists = userRepository.findByEmail(request.getEmail()).isPresent();

        if (userExists) throw new UserAlreadyExistsException("User already exists");

        Role role = roleRepository.findRoleByRoleName("ROLE_CUSTOMER").orElseThrow(() -> new RoleNotFoundException("Role cannot be found. Unable to create account"));

        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(List.of(role))
                .deleted(false)
                .activated(true)
                .fullName("Zalupka") //TODO change fullName when we decide to open registration for regular customer
                .build();

        userRepository.save(newUser);

        Map<String, Object> claims = new HashMap<>();

        claims.put("role", role.getRoleName());

        String token = jwtService.createToken(claims, newUser);

        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    public AuthenticationResponse authenticate(UserAuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                ));

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        Map<String, Object> claims = new HashMap<>();

        claims.put("roles", user.getRoles().stream().map(Role::getRoleName).toList());

        String token = jwtService.createToken(claims, user);

        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    public RecoveryResponse startRecovery(StartRecoveryRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new UsernameNotFoundException("User with such email wasn't found")
        );

        String activationCode = UUID.randomUUID().toString();

        user.setActivationCode(activationCode);

        userRepository.save(user);

        emailService.sendSimpleMessage(request.getEmail(), "Password recovery", String.format("Your recovery link: %s", activationCode));

        return RecoveryResponse.builder()
                .message("Recovery link was sent to email")
                .build();
    }

    public RecoveryResponse recovery(RecoveryRequest request) {
        User user = userRepository.findByActivationCode(request.getActivationCode()).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        return RecoveryResponse.builder()
                .message("Password was successfully reset")
                .build();
    }

    @Transactional
    public PersonalDataResponse savePersonalData(PersonalDataRequest request) {
        Optional<User> ifUser = userRepository.findByActivationCode(request.getActivationCode());

        if (ifUser.isEmpty()) throw new UsernameNotFoundException("User not found!");

        Address newAddress = AddressMapper.INSTANCE.toAddress(request.getAddress());
        newAddress = addressRepository.save(newAddress);

        User user = ifUser.get();
        user.setPersonalCode(request.getPersonalCode());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAddress(newAddress);
        user.setActivationCode(null);
        user.setPhone(formatService.formatE164(request.getPhone()));
        user.setActivated(true);
        List<Role> newRoles = new ArrayList<>(user.getRoles());
        newRoles.removeIf(role -> role.getRoleName().equals("ROLE_NEWBIE"));
        user.setRoles(newRoles);

        userRepository.save(user);

        return PersonalDataResponse.builder()
                .message("Personal data was successfully saved!")
                .build();
    }

    public ActivationCodeValidationResponse validateActivationCode(String id) {
        userRepository.findByActivationCode(id).orElseThrow(() -> new UsernameNotFoundException("Activation code not valid"));
        return ActivationCodeValidationResponse.builder()
                .message("Activation code valid")
                .build();
    }
}
