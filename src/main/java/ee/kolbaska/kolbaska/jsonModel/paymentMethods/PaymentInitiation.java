package ee.kolbaska.kolbaska.jsonModel.paymentMethods;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInitiation {
    private String processor;
    private Map<String, Country> setup;
}
