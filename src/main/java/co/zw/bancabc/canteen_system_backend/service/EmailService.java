package co.zw.bancabc.canteen_system_backend.service;

import co.zw.bancabc.canteen_system_backend.model.Order;
import co.zw.bancabc.canteen_system_backend.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /** Try to send; log content either way. Never throws. */
    @Async
    public void sendOrderConfirmation(Order order, User user) {
        String subject = String.format("Order #%d Confirmed", order.getId());
        String body = buildBody(order, user);

        boolean trySend = fromEmail != null && !fromEmail.isBlank();
        if (trySend) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(fromEmail);
                msg.setTo(user.getEmail());
                msg.setSubject(subject);
                msg.setText(body);
                mailSender.send(msg);
                log.info("Confirmation email sent to {} for order #{}", user.getEmail(), order.getId());
                return;
            } catch (Exception e) {
                log.warn("Email send failed for order #{}: {}. Falling back to log.", order.getId(), e.getMessage());
            }
        } else {
            log.debug("Mail not configured. Logging confirmation content instead.");
        }

        // Fallback — log the email content
        log.info("\n===== ORDER CONFIRMATION EMAIL =====\nTo: {}\nSubject: {}\n\n{}\n====================================",
                user.getEmail(), subject, body);
    }

    private String buildBody(Order order, User user) {
        return String.format("""
                Dear %s,

                Your order has been confirmed.

                Order ID:     #%d
                Meal:         %s
                Amount:       $%s
                Drink:        %s
                Drink Price:  $%s
                Total:        $%s
                Order Date:   %s
                Confirmed At: %s

                Thank you for using the canteen service.
                """,
                user.getUsername(),
                order.getId(),
                order.getMealName(),
                fmt(order.getAmount()),
                order.isHasDrink() ? (order.getDrinkName() != null ? order.getDrinkName() : "Yes") : "No",
                fmt(order.getDrinkPrice()),
                fmt(order.getTotalAmount()),
                order.getOrderDate(),
                order.getConfirmedAt());
    }

    private String fmt(BigDecimal v) {
        return v == null ? "0.00" : String.format("%.2f", v);
    }
}