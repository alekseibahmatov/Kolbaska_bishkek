package ee.kolbaska.kolbaska.model.payment;

import ee.kolbaska.kolbaska.model.baseentity.UUIDModel;
import ee.kolbaska.kolbaska.model.business.PaymentCustomer;
import ee.kolbaska.kolbaska.model.certificate.Certificate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "payment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends UUIDModel {

    @NotNull
    @Column(
            name = "from_email",
            columnDefinition = "varchar(120)",
            nullable = false
    )
    private String fromEmail;

    @NotNull
    @Column(
            name = "from_fullname",
            columnDefinition = "varchar(120)",
            nullable = false
    )
    private String fromFullName;

    @NotNull
    @Column(
            name = "payment_status",
            nullable = false
    )
    private Status status;

    @NotNull
    @Column(
            name = "merchant_reference",
            nullable = false
    )
    private String merchantReference;

    @OneToMany(mappedBy = "payment", orphanRemoval = true)
    private Set<Certificate> certificates;

    @OneToMany(mappedBy = "payment", orphanRemoval = true)
    private Set<PaymentCustomer> paymentCustomers = new LinkedHashSet<>();

}
