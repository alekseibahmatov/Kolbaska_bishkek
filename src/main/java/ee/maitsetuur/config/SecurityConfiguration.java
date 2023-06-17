package ee.maitsetuur.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter authenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Value("${api.basepath}")
    private String API_BASEPATH;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable()
                .authorizeHttpRequests(
                        (requests) -> requests
                                .requestMatchers(
                                        API_BASEPATH + "/auth/**",
                                        API_BASEPATH + "/payment/initiateCreation",
                                        API_BASEPATH + "/customer/**",
                                        API_BASEPATH + "/payment/methods",
                                        API_BASEPATH + "/payment/validatePayment"
                                ).permitAll()
                                .requestMatchers(
                                        API_BASEPATH + "/payment/verificationCreation"
                                ).access(hasIpAddress(List.of("35.156.245.42", "35.156.159.169")))
                                .requestMatchers(
                                        "/api-docs",
                                        "/api-docs/**",
                                        "/configuration/**",
                                        "/swagger*/**",
                                        "/webjars/**",
                                        "/actuator",
                                        "/actuator/**"
                                ).permitAll()
                                .requestMatchers(
                                        API_BASEPATH + "/admin/**",
                                        API_BASEPATH + "/admin/download/**",
                                        API_BASEPATH + "/admin/file/**"
                                ).hasRole("ADMIN")
                                .requestMatchers(
                                        API_BASEPATH + "/accountant/**"
                                ).hasAnyRole("ADMIN", "MANAGER")
                                .requestMatchers(
                                        API_BASEPATH + "/transaction/**"
                                ).hasAnyRole("ADMIN", "MANAGER", "WAITER")
                                .requestMatchers(
                                        API_BASEPATH + "/restaurant/**"
                                ).hasAnyRole("ADMIN", "MANAGER")
                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new CorsFilter(), ChannelProcessingFilter.class);
        return http.build();
    }

    private static AuthorizationManager<RequestAuthorizationContext> hasIpAddress(List<String> ipAddress) {
        List<IpAddressMatcher> ipAddressMatchers = ipAddress.stream()
                .map(IpAddressMatcher::new)
                .toList();

        return (authentication, context) -> {
            String request = context.getRequest().getHeader("x-forwarded-for");
            return new AuthorizationDecision(
                    ipAddressMatchers.stream()
                            .anyMatch(matcher -> matcher.matches(request))
            );
        };
    }

    @Bean
    public FilterRegistrationBean<RequestContextFilter> requestContextFilter() {
        FilterRegistrationBean<RequestContextFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new RequestContextFilter());
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }
}
