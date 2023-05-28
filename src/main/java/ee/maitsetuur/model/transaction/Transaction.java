package ee.maitsetuur.model.transaction;

import ee.maitsetuur.model.baseentity.UUIDModel;
import ee.maitsetuur.model.certificate.Certificate;
import ee.maitsetuur.model.report.Report;
import ee.maitsetuur.model.restaurant.Restaurant;
import ee.maitsetuur.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends UUIDModel {

    @NotNull
    @Column(
            name = "value",
            nullable = false
    )
    private Double value;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private User waiter;

    @ManyToOne(optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "certificate_id", nullable = false)
    private Certificate certificate;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

}
