package ee.kolbaska.kolbaska.request;

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
public class AdminCustomerUpdateRequest {

    @NotNull
    private Long id;

    @NotNull
    private String fullName;

    private String newPassword;

    @Email
    private String email;

    @Size(min = 12, max = 15, message = "Incorrect phone number")
    private String phone;

    @NotNull
    private AddressRequest address;

    @NotNull
    private String personalCode;

    @NotNull
    private Boolean activated;

    @NotNull
    private Boolean deleted;

    @NotNull
    private String activationCode;

    @NotNull
    private String restaurantCode;

    @NotNull
    private List<String> roleNames;
}
