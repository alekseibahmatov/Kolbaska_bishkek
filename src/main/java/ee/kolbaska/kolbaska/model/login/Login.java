package ee.kolbaska.kolbaska.model.login;

import ee.kolbaska.kolbaska.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "login")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Login {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(
            name = "ip",
            columnDefinition = "varchar(15)",
            nullable = false,
            updatable = false
    )
    private String ip;

    @Column(
            name = "user_agent",
            columnDefinition = "text",
            nullable = false
    )
    private String userAgent;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(
            name = "created_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Date createdAt;

}
