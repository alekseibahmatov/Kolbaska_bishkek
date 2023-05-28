package ee.maitsetuur.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertificateActivationRequest {
    @NotNull
    private UUID uniqueCode;

    @NotNull
    private Double amount;
}
