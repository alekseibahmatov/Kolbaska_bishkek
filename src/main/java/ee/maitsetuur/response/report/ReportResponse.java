package ee.maitsetuur.response.report;

import ee.maitsetuur.model.payment.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponse {
    private UUID id;

    private Integer transactionsAmount;

    private Double turnover;

    private Double maitsetuurShare;

    private LocalDate reportFrom;

    private LocalDate reportTo;

    private Status status;

    private Set<ReportTransactionResponse> reportTransactionResponse;
}
