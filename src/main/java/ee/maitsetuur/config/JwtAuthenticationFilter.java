package ee.maitsetuur.config;

import ee.maitsetuur.model.login.Login;
import ee.maitsetuur.model.user.User;
import ee.maitsetuur.repository.LoginRepository;
import ee.maitsetuur.repository.UserRepository;
import ee.maitsetuur.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final LoginRepository loginRepository;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUserEmail(jwt);

        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);

                String clientIpAddress = request.getHeader("X-FORWARDED-FOR");
                if (clientIpAddress == null) {
                    clientIpAddress = request.getRemoteAddr();
                }

                String userAgent = request.getHeader("User-Agent");

                User loginUser = userRepository.findByEmail(userEmail).get();

                Login newLogin = Login.builder()
                        .ip(clientIpAddress)
                        .userAgent(userAgent)
                        .user(loginUser)
                        .build();

                if (loginUser.getLogins() == null) loginUser.setLogins(List.of(newLogin));
                else {
                    List<Login> logins = new ArrayList<>(loginUser.getLogins());
                    logins.add(newLogin);

                    loginUser.setLogins(logins);
                }

                loginRepository.save(newLogin);
                userRepository.save(loginUser);
            }
        }
        filterChain.doFilter(request, response);
    }
}
