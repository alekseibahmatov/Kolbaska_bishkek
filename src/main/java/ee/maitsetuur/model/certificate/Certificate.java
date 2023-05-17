package ee.maitsetuur.model.certificate;

import ee.maitsetuur.model.baseentity.UUIDModel;
import ee.maitsetuur.model.payment.Payment;
import ee.maitsetuur.model.transaction.Transaction;
import ee.maitsetuur.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "certificate")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Certificate extends UUIDModel {

    @NotNull
    @Column(
            name = "greeting",
            columnDefinition = "varchar(120)",
            nullable = false
    )
    private String greeting;

    @NotNull
    @Column(
            name = "greeting_text",
            columnDefinition = "text",
            nullable = false
    )
    private String greetingText;

    @NotNull
    @Column(
            name = "value",
            columnDefinition = "double",
            nullable = false
    )
    private Double value;

    @Column(
            name = "remaining_value",
            columnDefinition = "double"
    )
    private Double remainingValue;

    @NotNull
    @Column(
            name = "valid_until",
            columnDefinition = "date",
            nullable = false
    )
    private LocalDate validUntil;

    @NotNull
    @Column(
            name = "created_by_admin",
            columnDefinition = "bool",
            nullable = false
    )
    private Boolean createdByAdmin;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_holder")
    private User holder;

    @NotNull
    @Column(
            name = "active",
            columnDefinition = "bool",
            nullable = false
    )
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "user_sender")
    private User sender;

    @OneToMany(mappedBy = "certificate", orphanRemoval = true)
    private List<Transaction> transactions;

    @ManyToOne(optional = true)
    @JoinColumn(name = "payment_id", nullable = true, updatable = false)
    private Payment payment;

}
