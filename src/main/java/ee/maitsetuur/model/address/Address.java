package ee.maitsetuur.model.address;

import ee.maitsetuur.model.baseentity.DefaultModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "address")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address extends DefaultModel {

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
            columnDefinition = "varchar(20)"
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
