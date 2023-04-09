package ee.kolbaska.kolbaska.model.payment;

import ee.kolbaska.kolbaska.model.baseentity.UUIDModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "payment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends UUIDModel {

    @NotNull
    @Column(
            name = "from_email",
            columnDefinition = "varchar(120)",
            nullable = false
    )
    private String fromEmail;

    @NotNull
    @Column(
            name = "to_email",
            columnDefinition = "varchar(120)",
            nullable = false
    )
    private String toEmail;

    @NotNull
    @Column(
            name = "description",
            columnDefinition = "text",
            nullable = false
    )
    private String description;

    @NotNull
    @Column(
            name = "to_fullname",
            columnDefinition = "varchar(120)",
            nullable = false
    )
    private String toFullName;

    @NotNull
    @Column(
            name = "from_fullname",
            columnDefinition = "varchar(120)",
            nullable = false
    )
    private String fromFullName;

    @NotNull
    @Column(
            name = "phone",
            columnDefinition = "varchar(15)",
            nullable = false
    )
    private String phone;

    @NotNull
    @Column(
            name = "value",
            columnDefinition = "int",
            nullable = false
    )
    private Integer value;

    @NotNull
    @Column(
            name = "payment_status",
            nullable = false
    )
    private Status status;
}
