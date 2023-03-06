package ee.kolbaska.kolbaska.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponse {
    private String street;

    private String apartmentNumber;

    private String city;

    private String state;

    private String zipCode;

    private String country;
}

