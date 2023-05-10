package ee.kolbaska.kolbaska.jsonModel.paymentData;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentIntent {
    @JsonProperty(required = true)
    private String uuid;

    @JsonProperty(required = true)
    private String paymentMethodType;

    @JsonProperty(required = true)
    private Map<String, String> paymentMethodMetadata;

    @JsonProperty(required = true)
    private BigDecimal amount;

    @JsonProperty(required = true)
    private String currency;

    @JsonProperty(required = true)
    private String status;

    @JsonProperty(required = true)
    private BigDecimal serviceFee;

    @JsonProperty(required = true)
    private String serviceFeeCurrency;

    @JsonProperty(required = true)
    private LocalDateTime createdAt;
}
