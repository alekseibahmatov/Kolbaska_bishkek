package ee.kolbaska.kolbaska.model.transaction;

import ee.kolbaska.kolbaska.model.certificate.Certificate;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "transaction")
@Getter
@Setter
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
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Date updatedAt;

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
