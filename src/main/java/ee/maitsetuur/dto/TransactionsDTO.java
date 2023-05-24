package ee.maitsetuur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionsDTO {
    private String uuid;

    private String activationDate;

    private String activationTime;

    private String waiterFullName;

    private String spent;
}
