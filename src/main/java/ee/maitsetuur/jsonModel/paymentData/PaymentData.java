package ee.maitsetuur.jsonModel.paymentData;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentData {
    @JsonProperty(required = true)
    private String uuid;

    @JsonProperty(required = true)
    private String paymentStatus;

    @JsonProperty(required = true)
    private String locale;

    @JsonProperty(required = true)
    private String merchantReference;

    @JsonProperty(required = true)
    private String merchantReferenceDisplay;

    @JsonProperty(required = true)
    private String merchantReturnUrl;

    @JsonProperty(required = true)
    private String merchantNotificationUrl;

    @JsonProperty(required = true)
    private BigDecimal grandTotal;

    @JsonProperty(required = true)
    private String currency;

    @JsonProperty(required = true)
    private String paymentMethodType;

    @JsonProperty("paymentIntents")
    private List<PaymentIntent> paymentIntents;

    @JsonProperty(required = true)
    private List<Refund> refunds;

    @JsonProperty(required = true)
    private List<LineItem> lineItems;

    @JsonProperty(required = true)
    private Address billingAddress;

    @JsonProperty(required = true)
    private Address shippingAddress;

    @JsonProperty(required = true)
    private String expiresAt;

    @JsonProperty(required = true)
    private String createdAt;

    @JsonProperty(required = true)
    private String storeName;

    @JsonProperty(required = true)
    private String businessName;

    @JsonProperty(required = true)
    private String paymentUrl;
}

