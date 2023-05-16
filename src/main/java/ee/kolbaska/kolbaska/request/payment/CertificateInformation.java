package ee.kolbaska.kolbaska.request.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CertificateInformation {
    @NotNull
    private String email;

    @NotNull
    private String greeting;

    @NotNull
    private Double nominalValue;

    @NotNull
    private String greetingsText;
}
