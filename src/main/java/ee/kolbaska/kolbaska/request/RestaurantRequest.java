package ee.kolbaska.kolbaska.request;

import ee.kolbaska.kolbaska.model.file.File;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantRequest {
    @NotNull
    private String restaurantName;

    @NotNull
    private String restaurantDescription;

    @NotNull
    @Email
    private String restaurantEmail;

    @NotNull
    @Email
    private String managerEmail;

    @NotNull
    @Size(min = 12, max = 15)
    private String restaurantPhone;

    @NotNull
    private String country;

    @NotNull
    private String state;

    @NotNull
    private String street;

    @NotNull
    private String city;

    @NotNull
    @Size(max = 10)
    private String postalCode;

    @NotNull
    @Size(max = 120)
    private String workingHours;

    @NotNull
    private Integer averageBill;

    @NotNull
    private List<String> categories;

    @NotNull
    private MultipartFile photo;

    @NotNull
    private MultipartFile contact;
}
