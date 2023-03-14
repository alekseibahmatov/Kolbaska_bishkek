package ee.kolbaska.kolbaska.request;

import jakarta.validation.constraints.NotNull;
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
    private Long holderUserId;

    @NotNull
    private Integer value;

    @NotNull
    private Date validUntil;

    @NotNull
    private String description;

}
