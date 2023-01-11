package ee.kolbaska.kolbaska.model.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Entity

@Table(name = "role")
@Getter
@Setter
public class Role {

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

    @Column(name = "role_name", nullable = false)
    private String roleName;
}
