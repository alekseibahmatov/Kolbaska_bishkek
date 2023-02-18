package ee.kolbaska.kolbaska.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertificateCreationRequest {
    @NotNull
    private Integer value;

    @NotNull
    @Email
    private String fromEmail;

    @NotNull
    private String toFullName;

    @NotNull
    private String fromFullName;

    @NotNull
    @Email
    private String toEmail;

    @NotNull
    private String toPhone;

    @NotNull
    private String fromPhone;

    @NotNull
    private String congratsText;

    @NotNull
    private AddressRequest billingAddress;

    @NotNull
    private AddressRequest shippingAddress;

    private String preferredProvider; //TODO change this to mandatory when will connect montonio
}
