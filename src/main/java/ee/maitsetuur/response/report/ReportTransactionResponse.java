package ee.maitsetuur.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportTransactionResponse {
    private String uuid;

    private LocalDate activationDate;

    private LocalTime activationTime;

    private String waiterFullName;

    private Double amountSpent;
}
