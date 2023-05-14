package ee.kolbaska.kolbaska.model.business;

import ee.kolbaska.kolbaska.model.baseentity.DefaultModel;
import ee.kolbaska.kolbaska.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "business")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Business extends DefaultModel {
    @NotNull
    @Column(
            name = "business_name",
            nullable = false
    )
    private String businessName;

    @NotNull
    @Column(
            name = "register_code",
            nullable = false
    )
    private Integer registerCode;

    @NotNull
    @Column(
            name = "business_kmkr",
            nullable = false
    )
    private String businessKMKR;


    @ManyToOne(optional = false)
    @JoinColumn(name = "representative_id", nullable = false)
    private User representative;

}
