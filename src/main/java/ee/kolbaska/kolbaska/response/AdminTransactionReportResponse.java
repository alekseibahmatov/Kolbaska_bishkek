package ee.kolbaska.kolbaska.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
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

    private Date startFrom;

    private Date endTo;

}
