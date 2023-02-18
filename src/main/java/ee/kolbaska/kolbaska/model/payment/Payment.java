package ee.kolbaska.kolbaska.model.payment;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;

@Entity
@Table(name = "payment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

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
}
