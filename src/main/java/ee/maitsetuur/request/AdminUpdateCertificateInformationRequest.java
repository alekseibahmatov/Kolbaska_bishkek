package ee.maitsetuur.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUpdateCertificateInformationRequest {

    @NotNull
    private UUID id;

    private Long senderUserId;

    @NotNull
    private Long holderUserId;

    @NotNull
    private Double value;

    @NotNull
    private Double remainingValue;

    @NotNull
    private LocalDate validUntil;

    @NotNull
    private String description;
}
