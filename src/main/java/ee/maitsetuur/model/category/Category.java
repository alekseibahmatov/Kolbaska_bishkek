package ee.maitsetuur.model.category;

import ee.maitsetuur.model.baseentity.DefaultModel;
import ee.maitsetuur.model.restaurant.Restaurant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "category")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category extends DefaultModel {

    @NotNull
    @Column(
            name = "name",
            columnDefinition = "varchar(32)",
            nullable = false
    )
    private String name;

    @ManyToMany(mappedBy = "categories")
    private List<Restaurant> restaurants;

}
