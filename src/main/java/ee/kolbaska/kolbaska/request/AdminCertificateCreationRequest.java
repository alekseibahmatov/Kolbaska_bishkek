package ee.kolbaska.kolbaska.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminCertificateCreationRequest {

    @NotNull
    private String toFullName;

    @NotNull
    @Email
    private String toEmail;

    @NotNull
    @Size(min = 12, max = 15)
    private String toPhone;

    @NotNull
    private Integer value;

    @NotNull
    private Date validUntil;

    @NotNull
    private String description;

    @NotNull
    private AddressRequest address;
}
