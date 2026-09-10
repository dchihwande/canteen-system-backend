package co.zw.bancabc.canteen_system_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(name = "ldap_dn", length = 500)
    private String ldapDn;

    @Column(name = "is_cashier")
    private boolean isCashier = false;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "password_hash", length = 200)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User(String username, String email, String ldapDn) {
        this.username = username;
        this.email = email;
        this.ldapDn = ldapDn;
    }

    public void setRawPassword(String raw, org.springframework.security.crypto.password.PasswordEncoder encoder) {
        this.passwordHash = encoder.encode(raw);
    }
}