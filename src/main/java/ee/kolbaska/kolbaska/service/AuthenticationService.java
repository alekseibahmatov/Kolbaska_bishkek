package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.repository.RoleRepository;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.request.AuthenticationRequest;
import ee.kolbaska.kolbaska.request.RegisterRequest;
import ee.kolbaska.kolbaska.response.AuthenticationResponse;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) throws Exception {
        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(
                        roleRepository.findRoleByRoleName("ROLE_CUSTOMER")
                        .orElseThrow(() -> new RoleNotFoundException("Role cannot be found. Unable to create account"))
                )
                .deleted(false)
                .activated(true)
                .fullName("Zalupka")
                .build();
        userRepository.save(newUser);

        String token = jwtService.createToken(newUser);

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

        String token = jwtService.createToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }
}
