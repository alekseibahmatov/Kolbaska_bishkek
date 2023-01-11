package ee.kolbaska.kolbaska.model.category;

import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "category")
@Getter
@Setter
public class Category {

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

    @NotNull
    @Column(
            name = "name",
            columnDefinition = "varchar(32)",
            nullable = false
    )
    private String name;

    @NotNull
    @Column(
            name = "description",
            columnDefinition = "text",
            nullable = false
    )
    private String description;


    @ManyToMany
    @JoinTable(name = "category_restaurants",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurants_id"))
    private List<Restaurant> restaurants = new ArrayList<>();
}
