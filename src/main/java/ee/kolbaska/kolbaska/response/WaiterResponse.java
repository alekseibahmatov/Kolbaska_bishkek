package ee.kolbaska.kolbaska.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WaiterResponse {

    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private Double turnover;

    private String message;

    public WaiterResponse(String message) {
        this.message = message;
    }
}
