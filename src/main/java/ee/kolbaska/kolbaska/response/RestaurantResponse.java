package ee.kolbaska.kolbaska.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantResponse {

    private Long id;

    private String restaurantName;

    private String restaurantEmail;

    private Double receivedTotal;

    private String message;

}
