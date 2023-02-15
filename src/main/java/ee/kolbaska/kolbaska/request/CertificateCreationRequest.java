package ee.kolbaska.kolbaska.request;

import ee.kolbaska.kolbaska.model.address.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertificateCreationRequest {
    private Integer value;

    private String fromEmail;

    private String toFullName;

    private String toEmail;

    private String toPhone;

    private String congratsText;

    private Address billingAddress;

    private Address shippingAddress;

    private String preferredProvider;
}
