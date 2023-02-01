package ee.kolbaska.kolbaska.model.transaction;

import ee.kolbaska.kolbaska.model.certificate.Certificate;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(
            name = "created_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Date createdAt;

    @Column(
            name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
            insertable = false,
            updatable = false
    )
    private Date updatedAt;

    @Column(
            name = "deleted_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Date deletedAt;

    @NotNull
    @Column(
            name = "value",
            columnDefinition = "double(4,2)",
            nullable = false
    )
    private Double value;


    @NotNull
    @OneToOne(optional = false)
    @JoinColumn(nullable = false)
    private Certificate certificate;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private User waiter;


    @ManyToOne(optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

}
