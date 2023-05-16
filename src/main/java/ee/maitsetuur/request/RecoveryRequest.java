package ee.maitsetuur.request;

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
public class RecoveryRequest {
    @NotNull
    private String activationCode;

    @NotNull
    @Size(min = 8, max = 12)
    private String newPassword;
}
