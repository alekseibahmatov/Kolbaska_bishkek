package ee.maitsetuur.model.user;

import ee.maitsetuur.model.address.Address;
import ee.maitsetuur.model.baseentity.DefaultModel;
import ee.maitsetuur.model.business.Business;
import ee.maitsetuur.model.certificate.Certificate;
import ee.maitsetuur.model.login.Login;
import ee.maitsetuur.model.restaurant.Restaurant;
import ee.maitsetuur.model.transaction.Transaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends DefaultModel implements UserDetails {


    @Column(
            name = "password",
            columnDefinition = "varchar(60)"
    )
    @Size(min = 60, max = 60)
    private String password;

    @Column(
            name = "full_name",
            columnDefinition = "varchar(60)"
    )
    private String fullName;

    @NotNull
    @Column(
            name = "email",
            columnDefinition = "varchar(120)",
            nullable = false
    )
    @Email
    private String email;

    @Column(
            name = "phone",
            columnDefinition = "varchar(15)"
    )
    @Size(max = 15)
    private String phone;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @Column(
            name = "personal_code",
            columnDefinition = "varchar(11)"
    )
    private String personalCode;

    @NotNull
    @Column(
            name = "activated",
            columnDefinition = "bool",
            nullable = false
    )
    private Boolean activated;

    @Column(
            name = "activation_code",
            columnDefinition = "varchar(36)"
    )
    private String activationCode;

    @OneToMany
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "holder", orphanRemoval = true)
    private List<Certificate> receivedCertificates;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @OneToOne(mappedBy = "manager")
    private Restaurant managedRestaurant;

    @OneToMany(mappedBy = "user", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Login> logins;

    @OneToMany(mappedBy = "sender", orphanRemoval = true)
    private List<Certificate> sent_certificates;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "roles_id"))
    private Collection<Role> roles;

    @OneToMany(mappedBy = "representative", orphanRemoval = true)
    private List<Business> businesses;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getRoleName())).toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isDeleted();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activated;
    }
}
