package co.zw.bancabc.canteen_system_backend.repository;

import co.zw.bancabc.canteen_system_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByLdapDn(String ldapDn);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}