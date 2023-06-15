package ee.maitsetuur.model.business;

import ee.maitsetuur.model.baseentity.DefaultModel;
import ee.maitsetuur.model.payment.Payment;
import ee.maitsetuur.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

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

    @Column(
            name = "business_kmkr"
    )
    private String businessKMKR;


    @ManyToOne(optional = false)
    @JoinColumn(name = "representative_id", nullable = false)
    private User representative;

    @OneToMany(mappedBy = "business", orphanRemoval = true)
    private Set<Payment> payments;

}
