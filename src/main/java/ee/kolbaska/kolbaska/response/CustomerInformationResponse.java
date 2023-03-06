package ee.kolbaska.kolbaska.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInformationResponse {
    private String fullName;

    private String email;

    private String phone;

    private AddressResponse address;

    private String personalCode;

    private Boolean activated;

    private Boolean deleted;

    private String activationCode;

    private List<TransactionResponse> transactions;

    private Long restaurantId;

    private List<LoginResponse> logins;

    private List<String> roleNames;
}
