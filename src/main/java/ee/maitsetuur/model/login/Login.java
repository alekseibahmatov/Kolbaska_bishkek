package ee.maitsetuur.model.login;

import ee.maitsetuur.model.baseentity.DefaultModel;
import ee.maitsetuur.model.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "login")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Login extends DefaultModel {

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

}
