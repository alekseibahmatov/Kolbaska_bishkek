package ee.maitsetuur.request;

import jakarta.validation.constraints.Email;
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
public class UserCreationRequest {
    @NotNull
    private String fullName;

    private String password;

    @NotNull
    @Email
    private String email;

    @Size(max = 15)
    private String phone;

    @NotNull
    private AddressRequest address;

    private String personalCode;

    private String roleName;
}
