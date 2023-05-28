package ee.maitsetuur.jsonModel.paymentData;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LineItem {
    private String name;

    private Integer quantity;

    private Double finalPrice;
}
