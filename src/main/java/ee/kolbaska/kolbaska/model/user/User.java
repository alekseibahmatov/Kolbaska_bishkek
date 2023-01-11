package ee.kolbaska.kolbaska.model.user;

import ee.kolbaska.kolbaska.model.address.Address;
import ee.kolbaska.kolbaska.model.certificate.Certificate;
import ee.kolbaska.kolbaska.model.restaurant.Restaurant;
import ee.kolbaska.kolbaska.model.transaction.Transaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
public class User {

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
    @Column(
            name = "username",
            columnDefinition = "varchar(24)"
    )
    @Size(min = 4, max = 24)
    private String username;

    @Column(
            name = "password",
            columnDefinition = "varchar(60)"
    )
    @Size(min = 60, max = 60)
    private String password;

    @NotNull
    @Column(
            name = "full_name",
            columnDefinition = "varchar(60)",
            nullable = false
    )
    private String fullName;

    @Column(
            name = "email",
            columnDefinition = "varchar(120)"
    )
    @Email
    private String email;

    @Column(
            name = "phone",
            columnDefinition = "varchar(15)"
    )
    @Size(min = 15, max = 15)
    private String phone;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @Column(
            name = "last_logged",
            columnDefinition = "date"
    )
    private Date lastLogged;

    @Column(
            name = "last_ip",
            columnDefinition = "varchar(15)"
    )
    private String lastIp;

    @NotNull
    @Column(
            name = "activated",
            columnDefinition = "bool",
            nullable = false
    )
    private Boolean activated;

    @NotNull
    @Column(
            name = "deleted",
            columnDefinition = "bool",
            nullable = false
    )
    private Boolean deleted;

    @OneToMany
    private List<Transaction> transactions;

    @OneToMany
    private List<Certificate> certificates = new java.util.ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

}
