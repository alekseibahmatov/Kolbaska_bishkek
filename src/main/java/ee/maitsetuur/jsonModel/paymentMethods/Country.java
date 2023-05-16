package ee.maitsetuur.jsonModel.paymentMethods;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Country {
    @JsonProperty("supportedCurrencies")
    private List<String> supportedCurrencies;

    @JsonProperty("paymentMethods")
    private List<PaymentMethod> paymentMethods;
}
