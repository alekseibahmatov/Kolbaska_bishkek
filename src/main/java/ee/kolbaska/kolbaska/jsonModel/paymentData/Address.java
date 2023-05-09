package ee.kolbaska.kolbaska.jsonModel.paymentData;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    @JsonProperty(required = true)
    private String firstName;

    @JsonProperty(required = true)
    private String lastName;

    @JsonProperty(required = true)
    private String email;

    @JsonProperty(required = true)
    private String phoneNumber;

    @JsonProperty(required = true)
    private String phoneCountry;

    @JsonProperty(required = true)
    private String addressLine1;

    @JsonProperty(required = true)
    private String addressLine2;

    @JsonProperty(required = true)
    private String locality;

    @JsonProperty(required = true)
    private String region;

    @JsonProperty(required = true)
    private String country;

    @JsonProperty(required = true)
    private String postalCode;

    @JsonProperty(required = true)
    private String companyName;

    @JsonProperty(required = true)
    private String companyLegalName;

    @JsonProperty(required = true)
    private String companyRegCode;

    @JsonProperty(required = true)
    private String companyVatNumber;
}
