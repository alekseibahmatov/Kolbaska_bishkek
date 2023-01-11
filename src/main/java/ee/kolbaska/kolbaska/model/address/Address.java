package ee.kolbaska.kolbaska.model.address;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "address")
@Getter
@Setter
public class Address {

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
            name = "street",
            columnDefinition = "varchar(100)",
            nullable = false
    )
    private String street;

    @NotNull
    @Column(
            name = "apartment_number",
            columnDefinition = "varchar(10)",
            nullable = false
    )
    private String apartmentNumber;

    @NotNull
    @Column(
            name = "city",
            columnDefinition = "varchar(50)",
            nullable = false
    )
    private String city;

    @Column(
            name = "state",
            columnDefinition = "varchar(2)"
    )
    private String state;

    @NotNull
    @Column(
            name = "zip_code",
            columnDefinition = "varchar(10)",
            nullable = false
    )
    private String zipCode;

    @NotNull
    @Column(
            name = "country",
            columnDefinition = "varchar(50)",
            nullable = false
    )
    private String country;
}
