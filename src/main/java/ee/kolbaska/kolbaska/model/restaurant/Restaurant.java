package ee.kolbaska.kolbaska.model.restaurant;

import ee.kolbaska.kolbaska.model.address.Address;
import ee.kolbaska.kolbaska.model.category.Category;
import ee.kolbaska.kolbaska.model.transaction.Transaction;
import ee.kolbaska.kolbaska.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "restaurant")
@Getter
@Setter
public class Restaurant {

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

    @OneToMany(mappedBy = "restaurant", orphanRemoval = true)
    private Set<Transaction> transactions = new LinkedHashSet<>();

    @NotNull
    @Column(
            name = "name",
            columnDefinition = "varchar(64)",
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

    @NotNull
    @Column(
            name = "working_hours",
            columnDefinition = "varchar(120)",
            nullable = false
    )
    private String workingHours;

    @NotNull
    @Column(
            name = "company",
            columnDefinition = "varchar(24)",
            nullable = false
    )
    private String company;

    @NotNull
    @Column(
            name = "registration_code",
            columnDefinition = "int",
            nullable = false
    )
    private Integer registrationCode;

    @NotNull
    @Column(
            name = "average_bill",
            columnDefinition = "int",
            nullable = false
    )
    private Integer averageBill;

    @NotNull
    @ManyToOne
    private Address address;

    @NotNull
    @Column(
            name = "phone",
            columnDefinition = "varchar(15)",
            nullable = false
    )
    @Size(min = 15, max = 15)
    private String phone;

    @NotNull
    @Column(
            name = "restaurant_code",
            columnDefinition = "varchar(6)",
            nullable = false
    )
    private String restaurantCode;

    @NotNull
    @ManyToMany
    @JoinTable(name = "restaurant_categories",
            joinColumns = @JoinColumn(name = "Restaurant_id", referencedColumnName = "id"))
    private List<Category> categories;

    @NotNull
    @OneToMany(mappedBy = "restaurant")
    private List<User> waiters = new java.util.ArrayList<>();

}