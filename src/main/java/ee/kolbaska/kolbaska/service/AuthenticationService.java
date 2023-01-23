package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.exception.UserAlreadyExistsException;
import ee.kolbaska.kolbaska.model.user.Role;
import ee.kolbaska.kolbaska.repository.RoleRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.AuthenticationRequest;
import ee.kolbaska.kolbaska.request.RegisterRequest;
import ee.kolbaska.kolbaska.response.AuthenticationResponse;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) throws Exception {

        boolean userExists = userRepository.findByEmail(request.getEmail()).isPresent();

        if (userExists) throw new UserAlreadyExistsException("User already exists");

        Role role = roleRepository.findRoleByRoleName("ROLE_CUSTOMER").orElseThrow(() -> new RoleNotFoundException("Role cannot be found. Unable to create account"));

        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .deleted(false)
                .activated(true)
                .fullName("Zalupka")
                .build();


        userRepository.save(newUser);

        Map<String, String> claims = new HashMap<>();

        claims.put("role", role.getRoleName());

        String token = jwtService.createToken(claims, newUser);

        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                ));

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        Map<String, String> claims = new HashMap<>();

        claims.put("role", user.getRole().getRoleName());

        String token = jwtService.createToken(claims, user);

        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }
}
