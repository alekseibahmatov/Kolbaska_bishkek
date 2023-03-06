package ee.kolbaska.kolbaska.config;

import ee.kolbaska.kolbaska.model.user.Role;
import ee.kolbaska.kolbaska.model.user.User;
import ee.kolbaska.kolbaska.repository.UserRepository;
import ee.kolbaska.kolbaska.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserConfiguration {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Bean
    public User getRequestUser() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

            final String jwtToken = request.getHeader("Authorization").substring(7);

            final String email = jwtService.extractUserEmail(jwtToken);

            return userRepository.findByEmail(email).orElseThrow(
                    () -> new UsernameNotFoundException("User with such email not found!")
            );
        } catch (IllegalStateException e) {
            return null;
        }
    }

    @Bean
    public List<String> getRoleNames(User user) {
        return user.getRoles().stream().map(Role::getRoleName).toList();
    }
}
