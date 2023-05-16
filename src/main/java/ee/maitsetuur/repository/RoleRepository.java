package ee.maitsetuur.repository;

import ee.maitsetuur.model.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findRoleByRoleName(String roleName);

    List<Role> findByRoleNameIn(List<String> roles);
}
