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
public class UserAuthenticationRequest {
    @NotNull
    @Email
    private String email;

    @NotNull
    @Size(min = 6, max = 12)
    private String password;
}
