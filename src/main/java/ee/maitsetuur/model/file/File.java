package ee.maitsetuur.model.file;

import ee.maitsetuur.model.baseentity.UUIDModel;
import ee.maitsetuur.model.restaurant.Restaurant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "file")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class File extends UUIDModel {

    @NotNull
    @Column(
            name = "file_type",
            columnDefinition = "varchar(20)",
            nullable = false
    )
    private FileType fileType;

    @NotNull
    @Column(
            name = "file_name",
            columnDefinition = "varchar(50)",
            nullable = false
    )
    private String fileName;

    @OneToOne
    private Restaurant restaurant;

}
