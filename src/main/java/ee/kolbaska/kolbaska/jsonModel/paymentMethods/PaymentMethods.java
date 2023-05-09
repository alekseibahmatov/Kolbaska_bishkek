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
public class PaymentMethods {
    @JsonProperty("paymentInitiation")
    private PaymentInitiation paymentInitiation;

    @JsonProperty("cardPayments")
    private MainPaymentResponse.CardPayments cardPayments;

    @JsonProperty("blik")
    private MainPaymentResponse.Blik blik;
}
