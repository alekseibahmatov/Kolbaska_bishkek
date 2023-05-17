package ee.maitsetuur.model.restaurant;

import ee.maitsetuur.model.address.Address;
import ee.maitsetuur.model.baseentity.DefaultModel;
import ee.maitsetuur.model.category.Category;
import ee.maitsetuur.model.file.File;
import ee.maitsetuur.model.report.Report;
import ee.maitsetuur.model.transaction.Transaction;
import ee.maitsetuur.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "restaurant")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant extends DefaultModel {

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
    @Size(min = 12, max = 15, message = "Wrong phone number")
    private String phone;

    @NotNull
    @Column(
            name = "email",
            columnDefinition = "varchar(120)",
            nullable = false
    )
    @Email
    private String email;

    @NotNull
    @Column(
            name = "active",
            columnDefinition = "bool",
            nullable = false
    )
    private Boolean active;

    @NotNull
    @Column(
            name = "restaurant_code",
            columnDefinition = "varchar(6)",
            nullable = false
    )
    private String restaurantCode;

    @NotNull
    @Column(
            name = "maitsetuur_share",
            columnDefinition = "int",
            nullable = false
    )
    private Integer maitsetuurShare;

    @NotNull
    @Column(
            name = "report_days",
            nullable = false
    )
    private String reportDays;

    @NotNull
    @ManyToMany(cascade = CascadeType.REMOVE)
    @JoinTable(name = "restaurant_categories",
            joinColumns = @JoinColumn(name = "restaurant_id", referencedColumnName = "id"))
    private List<Category> categories;

    @OneToMany(mappedBy = "restaurant")
    private List<User> waiters;

    @OneToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    @OneToOne
    @JoinColumn(name = "photo_id")
    private File photo;

    @OneToOne
    @JoinColumn(name = "contract_id")
    private File contract;

    @OneToMany(mappedBy = "restaurant", orphanRemoval = true)
    private Set<Report> reports;

    @OneToMany(mappedBy = "restaurant", orphanRemoval = true)
    private Set<Transaction> transactions;
}
