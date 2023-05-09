package ee.kolbaska.kolbaska.jsonModel.paymentMethods;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MainPaymentResponse {
    @JsonProperty(required = true)
    private String uuid;

    @JsonProperty(required = true)
    private String name;

    @JsonProperty("paymentMethods")
    private PaymentMethods paymentMethods;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CardPayments {
        private String processor;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Blik {
        private String processor;
    }
}

