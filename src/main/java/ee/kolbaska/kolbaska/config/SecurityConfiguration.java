package ee.kolbaska.kolbaska.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                                .requestMatchers(API_BASEPATH + "/auth/**", API_BASEPATH + "/payment/initiateCreation").permitAll()
                                .requestMatchers(API_BASEPATH + "/payment/verificationCreation").permitAll() //TODO change permit all to access from specific IPs
                                .requestMatchers("/api-docs", "/api-docs/**", "/configuration/**", "/swagger*/**", "/webjars/**", "/actuator", "/actuator/**").permitAll()
                                .requestMatchers(API_BASEPATH + "/admin/**", API_BASEPATH + "/admin/download/**", API_BASEPATH + "/admin/file/**").hasRole("ADMIN")
                                .requestMatchers(API_BASEPATH + "/transaction/**").hasAnyRole("ADMIN", "MANAGER", "WAITER")
                                .requestMatchers(API_BASEPATH + "/restaurant/**").hasAnyRole("ADMIN", "MANAGER")
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

    @Bean
    public FilterRegistrationBean<RequestContextFilter> requestContextFilter() {
        FilterRegistrationBean<RequestContextFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new RequestContextFilter());
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }
}
