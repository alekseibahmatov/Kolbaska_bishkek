package ee.maitsetuur.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminCertificateResponse {

    private String id;

    private Double value;

    private Double remainingValue;

    private String holder;

    private String sender;

    private String validUntil;
}
