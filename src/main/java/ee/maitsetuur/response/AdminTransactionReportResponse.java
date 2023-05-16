package ee.maitsetuur.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminTransactionReportResponse {
    private List<TransactionResponse> transactionResponses;

    private String overallTurnoverBrutto;

    private String overallIncomeBrutto;

    private String overallIncomeNetto;

    private LocalDate startFrom;

    private LocalDate endTo;

}
