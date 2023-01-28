package ee.kolbaska.kolbaska.config;

import ee.kolbaska.kolbaska.property.FileStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class FileStorageConfiguration {
}
