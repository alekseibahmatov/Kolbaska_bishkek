package ee.kolbaska.kolbaska.model.file;

import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "file")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private String id;

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
            columnDefinition = "varchar(50)", // todo: rebuild database
            nullable = false
    )
    private String fileName;

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

    @OneToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

}
