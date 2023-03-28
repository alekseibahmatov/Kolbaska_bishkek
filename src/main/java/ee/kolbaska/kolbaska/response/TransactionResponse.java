package ee.kolbaska.kolbaska.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private String id;

    private String value;

    private String restaurantName;

    private String restaurantCode;

    private Long waiterId;

    private String waiterEmail;

    private String profit;

    private Date createdAt;
}
