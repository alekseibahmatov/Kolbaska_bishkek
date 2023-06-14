package ee.maitsetuur.model.payment;

import ee.maitsetuur.model.baseentity.UUIDModel;
import ee.maitsetuur.model.business.Business;
import ee.maitsetuur.model.business.PaymentCustomer;
import ee.maitsetuur.model.certificate.Certificate;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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

    @Enumerated
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
    private Set<PaymentCustomer> paymentCustomers;

    @ManyToOne
    @JoinColumn(name = "business_id")
    private Business business;

}
