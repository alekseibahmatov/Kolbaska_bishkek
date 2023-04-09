package ee.kolbaska.kolbaska.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
    private LocalDate validUntil;

    @NotNull
    private String description;

}
