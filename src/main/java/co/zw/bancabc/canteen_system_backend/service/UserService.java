package co.zw.bancabc.canteen_system_backend.service;

import co.zw.bancabc.canteen_system_backend.model.User;
import co.zw.bancabc.canteen_system_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final LdapService ldapService;

    /** Get current authenticated user from DB. */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }
        return getUserByUsername(auth.getName());
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Called at order time (per business rule "track on order").
     * If the user already exists, update email/ldapDn if changed.
     * Otherwise create a new user, pulling email/DN from AD.
     */
    @Transactional
    public User findOrCreateFromAd(String username) {
        Optional<User> existing = userRepository.findByUsername(username);
        String email = null;
        String dn = null;

        if (existing.isEmpty()) {
            email = ldapService.getUserEmail(username);
            dn = ldapService.getUserDn(username);
        }

        if (existing.isPresent()) {
            User user = existing.get();
            return user;
        }

        User newUser = new User(username, email, dn);
        newUser.setActive(true);
        newUser.setCashier(false);
        User saved = userRepository.save(newUser);
        log.info("Created local user record for {} on first order", username);
        return saved;
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
}