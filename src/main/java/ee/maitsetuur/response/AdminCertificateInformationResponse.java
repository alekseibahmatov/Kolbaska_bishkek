package ee.maitsetuur.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminCertificateInformationResponse {
    private Long fromId;

    private Long toId;

    private Double value;

    private Double remainingValue;

    private LocalDate validUntil;

    private Instant createdAt;

    private String description;

    private List<TransactionResponse> transactions;
}
