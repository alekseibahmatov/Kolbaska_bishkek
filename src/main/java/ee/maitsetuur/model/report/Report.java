package ee.maitsetuur.model.report;

import ee.maitsetuur.model.baseentity.DefaultModel;
import ee.maitsetuur.model.payment.Status;
import ee.maitsetuur.model.restaurant.Restaurant;
import ee.maitsetuur.model.transaction.Transaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "report")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report extends DefaultModel {
    @NotNull
    @Column(
            name = "transactions_amount",
            nullable = false
    )
    private Integer transactionsAmount;

    @NotNull
    @Column(
            name = "turnover",
            nullable = false
    )
    private Double turnover;

    @NotNull
    @Column(
            name = "maitsetuur_share",
            nullable = false
    )
    private Double maitsetuurShare;

    @NotNull
    @Column(
            name = "report_from",
            nullable = false
    )
    private LocalDate reportFrom;

    @NotNull
    @Column(
            name = "report_to",
            nullable = false
    )
    private LocalDate reportTo;

    @NotNull
    @Column(
            name = "status",
            nullable = false
    )
    private Status status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "report", orphanRemoval = true)
    private Set<Transaction> transactions;

}
