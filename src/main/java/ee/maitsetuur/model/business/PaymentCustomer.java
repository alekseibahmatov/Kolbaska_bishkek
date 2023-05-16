package ee.maitsetuur.model.business;

import ee.maitsetuur.model.baseentity.DefaultModel;
import ee.maitsetuur.model.payment.Payment;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "payment_customer")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCustomer extends DefaultModel {
    @NotNull
    @Column(
            name = "email",
            nullable = false
    )
    private String email;

    @NotNull
    @Column(
            name = "greeting",
            nullable = false
    )
    private String greeting;

    @NotNull
    @Column(
            name = "value",
            nullable = false
    )
    private Double value;

    @NotNull
    @Column(
            name = "greeting_text",
            nullable = false
    )
    private String greetingText;

    @ManyToOne(optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

}
