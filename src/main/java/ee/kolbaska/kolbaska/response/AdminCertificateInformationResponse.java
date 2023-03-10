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
public class AdminCertificateInformationResponse {
    private String toFullName;

    private String toEmail;

    private String toPhone;

    private Integer value;

    private Double remainingValue;

    private String validUntil;

    private Date createdAt;

    private String description;

    private String fromFullName;

    private String fromEmail;

    private String fromPhone;

    private List<TransactionResponse> transactions;
}
