package ee.kolbaska.kolbaska.model.transaction;

import ee.kolbaska.kolbaska.model.certificate.Certificate;
import ee.kolbaska.kolbaska.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "transaction")
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue
    private Long id;

    @Column(
            name = "created_at",
            columnDefinition = "datetime",
            nullable = false
    )
    private Date createdAt;

    @Column(
            name = "updated_at",
            columnDefinition = "datetime",
            nullable = false
    )
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
    @NotNull
    @Column(
            name = "unique_identifier",
            columnDefinition = "varchar(36)",
            nullable = false
    )
    private String uID;

    @NotNull
    @Column(
            name = "value",
            columnDefinition = "double(4,2)",
            nullable = false
    )
    private Double value;

    //TODO Add restaurant

    @NotNull
    @OneToOne(optional = false)
    @JoinColumn(nullable = false)
    private Certificate certificate;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private User waiter;
}
