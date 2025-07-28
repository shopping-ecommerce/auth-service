package iuh.fit.se.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iuh.fit.se.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    Role findByName(String name);
}
