package ee.kolbaska.kolbaska.property;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file-storage")
@Getter
@Setter
public class FileStorageProperties {
    private String uploadPhotoDir;

    private String uploadContractDir;

}
