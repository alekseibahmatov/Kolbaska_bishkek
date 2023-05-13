package ee.kolbaska.kolbaska.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
@AllArgsConstructor
public class PaymentValidationRequest {
    @NotNull
    private String orderToken;
}
