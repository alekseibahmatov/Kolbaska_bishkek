package ee.kolbaska.kolbaska.jsonModel.paymentMethods;

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
public class PaymentMethod {
    private String name;
    private String logoUrl;

    @JsonProperty("supportedCurrencies")
    private List<String> supportedCurrencies;

    private Integer uiPosition;
    private String code;
}
