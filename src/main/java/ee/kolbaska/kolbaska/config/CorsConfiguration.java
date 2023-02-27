package ee.kolbaska.kolbaska.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    @Value("${api.basepath}")
    private String API_BASEPATH;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(API_BASEPATH).allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }
}
