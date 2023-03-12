package ee.kolbaska.kolbaska.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantResponse {

    @NotNull
    private String restaurantName;

    @NotNull
    private String restaurantDescription;

    @NotNull
    @Email
    private String restaurantEmail;

    @NotNull
    @Size(min = 12, max = 15)
    private String restaurantPhone;

    @NotNull
    private Long managerId;

    @NotNull
    private AddressResponse address;

    @NotNull
    @Size(max = 120)
    private String workingHours;

    @NotNull
    private Integer averageBill;

    @NotNull
    private List<String> categories;

    @NotNull
    private String photo;

    @NotNull
    private String contract;

    @NotNull
    private Boolean active;

}
