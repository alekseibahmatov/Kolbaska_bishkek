package ee.kolbaska.kolbaska.model.transaction;

import ee.kolbaska.kolbaska.model.baseentity.UUIDModel;
import ee.kolbaska.kolbaska.model.certificate.Certificate;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.user.User;
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
            columnDefinition = "double(6,2)",
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

}
