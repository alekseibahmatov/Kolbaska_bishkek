package ee.kolbaska.kolbaska.model.user;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "role")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

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

    @Column(name = "role_name", nullable = false)
    private String roleName;

    @OneToMany(mappedBy = "role", orphanRemoval = true)
    private List<User> users;

}
