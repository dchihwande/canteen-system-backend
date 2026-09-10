package co.zw.bancabc.canteen_system_backend.config;

import co.zw.bancabc.canteen_system_backend.model.Drink;
import co.zw.bancabc.canteen_system_backend.model.User;
import co.zw.bancabc.canteen_system_backend.repository.DrinkRepository;
import co.zw.bancabc.canteen_system_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DrinkRepository drinkRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${cashier.username}")
    private String cashierUsername;

    @Value("${cashier.password}")
    private String cashierPassword;

    @Value("${cashier.email}")
    private String cashierEmail;

    @Override
    public void run(String... args) {
        seedCashier();
        seedDrinks();
    }

    private void seedCashier() {
        if (userRepository.findByUsername(cashierUsername).isEmpty()) {
            User cashier = new User(cashierUsername, cashierEmail, null);
            cashier.setCashier(true);
            cashier.setActive(true);
            cashier.setPasswordHash(passwordEncoder.encode(cashierPassword));
            userRepository.save(cashier);
            log.info("Seeded local cashier account: {}", cashierUsername);
        } else {
            log.debug("Cashier account already exists: {}", cashierUsername);
        }
    }

    private void seedDrinks() {
        if (drinkRepository.count() == 0) {
            drinkRepository.save(new Drink(null, "Water",   new BigDecimal("0.50"), true, "Bottled water"));
            drinkRepository.save(new Drink(null, "Soda",    new BigDecimal("1.00"), true, "Carbonated soft drink"));
            drinkRepository.save(new Drink(null, "Juice",   new BigDecimal("1.50"), true, "Fresh fruit juice"));
            drinkRepository.save(new Drink(null, "Coffee",  new BigDecimal("1.00"), true, "Hot coffee"));
            log.info("Seeded default drink menu");
        }
    }
}