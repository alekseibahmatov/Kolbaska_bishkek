package ee.kolbaska.kolbaska.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertificateActivationRequest {
    @NotNull
    private String uniqueCode;

    @NotNull
    private Double amount;
}
