package ee.maitsetuur.request.payment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuyerInformation {
    @NotNull
    private String fromFullName;

    @NotNull
    private String fromEmail;

    @NotNull
    private String fromPhone;
}
