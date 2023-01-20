package ee.kolbaska.kolbaska.model.certificate;

import ee.kolbaska.kolbaska.model.category.Category;
import ee.kolbaska.kolbaska.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "certificate")
@Getter
@Setter
public class Certificate {

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
            name = "sent_from",
            columnDefinition = "varchar(120)",
            nullable = false
    )
    private String sentFrom;

    @NotNull
    @Column(
            name = "sent_to",
            columnDefinition = "varchar(120)",
            nullable = false
    )
    private String sentTo;

    @NotNull
    @Column(
            name = "description",
            columnDefinition = "text",
            nullable = false
    )
    private String description;

    @NotNull
    @Column(
            name = "activation_code",
            columnDefinition = "varchar(36)",
            nullable = false
    )
    private String activationCode;

    @NotNull
    @Column(
            name = "value",
            columnDefinition = "int",
            nullable = false
    )
    private Integer value;


    @NotNull
    @Column(
            name = "valid_until",
            columnDefinition = "date",
            nullable = false
    )
    private String validUntil;


    @NotNull
    @ManyToOne
    private Category restaurantCategory;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User holder;

    @NotNull
    @Column(
            name = "active",
            columnDefinition = "bool",
            nullable = false
    )
    private Boolean active;

    @NotNull
    @Column(
            name = "activated_at",
            columnDefinition = "date",
            nullable = false
    )
    private Date activatedAt;
}
