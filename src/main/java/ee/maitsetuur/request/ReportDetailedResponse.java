package ee.maitsetuur.request;

import ee.maitsetuur.model.payment.Status;
import ee.maitsetuur.response.report.ReportTransactionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportDetailedResponse {
    private List<ReportTransactionResponse> transactions;

    private String turnover;

    private String maitsetuurShare;

    private Status status;
}
