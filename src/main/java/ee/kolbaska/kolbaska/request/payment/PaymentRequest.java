package ee.kolbaska.kolbaska.request.payment;

import ee.kolbaska.kolbaska.request.AddressRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    @NotNull
    private BusinessInformation businessInformation;

    @NotNull
    private BuyerInformation buyer;

    @NotNull
    private AddressRequest address;

    @NotNull
    private List<CertificateInformation> certificates;
}
