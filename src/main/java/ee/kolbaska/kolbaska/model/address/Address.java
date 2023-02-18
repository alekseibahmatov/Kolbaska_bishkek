package ee.kolbaska.kolbaska.model.address;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "address")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(
            name = "deleted_at",
            insertable = false,
            updatable = false
    )
    private Date deletedAt;

    @NotNull
    @Column(
            name = "street",
            columnDefinition = "varchar(100)",
            nullable = false
    )
    private String street;

    @Column(
            name = "apartment_number",
            columnDefinition = "varchar(10)"
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
