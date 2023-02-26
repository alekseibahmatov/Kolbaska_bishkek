package ee.kolbaska.kolbaska.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalDataRequest {

    @NotNull
    private String activationCode;

    @NotNull
    private String fullName;

    @NotNull
    @Size(min = 12, max = 15)
    private String phone;

    @NotNull
    @Size(min = 11, max = 11)
    private String personalCode;

    @NotNull
    private AddressRequest address;

    @NotNull
    @Size(min = 8)
    private String password;

}
