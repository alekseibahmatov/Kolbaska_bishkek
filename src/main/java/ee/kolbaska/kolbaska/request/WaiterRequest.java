package ee.kolbaska.kolbaska.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaiterRequest {

    @NotNull
    private String fullName;

    @NotNull
    private String phone;

    @NotNull
    @Email
    private String email;

    @NotNull
    private String personalCode;

    @NotNull
    private String restaurantCode;
}
