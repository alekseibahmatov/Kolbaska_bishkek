package ee.maitsetuur.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantTableResponse {

    private String restaurantCode;

    private String restaurantName;

    private String restaurantEmail;

    private String restaurantPhone;

    private Integer averageBill;

    // TODO Add picture
}
