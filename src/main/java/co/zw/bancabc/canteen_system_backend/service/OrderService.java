package co.zw.bancabc.canteen_system_backend.service;

import co.zw.bancabc.canteen_system_backend.dto.OrderRequest;
import co.zw.bancabc.canteen_system_backend.dto.OrderResponse;
import co.zw.bancabc.canteen_system_backend.model.Drink;
import co.zw.bancabc.canteen_system_backend.model.Order;
import co.zw.bancabc.canteen_system_backend.model.User;
import co.zw.bancabc.canteen_system_backend.model.enums.LunchType;
import co.zw.bancabc.canteen_system_backend.model.enums.MealType;
import co.zw.bancabc.canteen_system_backend.model.enums.OrderStatus;
import co.zw.bancabc.canteen_system_backend.repository.DrinkRepository;
import co.zw.bancabc.canteen_system_backend.repository.OrderRepository;
import co.zw.bancabc.canteen_system_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DrinkRepository drinkRepository;
    private final UserService userService;
    private final EmailService emailService;

    @Value("${canteen.lunch.traditional-price}")
    private BigDecimal traditionalPrice;

    @Value("${canteen.lunch.western-price}")
    private BigDecimal westernPrice;

    @Value("${canteen.drink.default-price}")
    private BigDecimal defaultDrinkPrice;

    @Transactional
    public OrderResponse createOrder(OrderRequest req) {
        String username = currentUsername();
        // track-on-order: create local user if not present
        User user = userService.findOrCreateFromAd(username);

        Order order = new Order();
        order.setUser(user);
        order.setMealType(req.getMealType());
        order.setHasDrink(Boolean.TRUE.equals(req.getHasDrink()));
        order.setNotes(req.getNotes());
        order.setStatus(OrderStatus.PENDING);

        if (req.getMealType() == MealType.BREAKFAST) {
            if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Breakfast amount must be greater than 0");
            }
            order.setAmount(req.getAmount());
            order.setMealName("Breakfast");
        } else if (req.getMealType() == MealType.LUNCH) {
            if (req.getLunchType() == null) {
                throw new IllegalArgumentException("Lunch type is required (TRADITIONAL or WESTERN)");
            }
            if (req.getLunchType() == LunchType.TRADITIONAL) {
                order.setAmount(traditionalPrice);
                order.setMealName("Traditional Lunch");
            } else {
                order.setAmount(westernPrice);
                order.setMealName("Western Lunch");
            }
            order.setLunchType(req.getLunchType());
        } else {
            throw new IllegalArgumentException("Invalid meal type");
        }

        if (order.isHasDrink()) {
            if (req.getDrinkId() == null) {
                throw new IllegalArgumentException("Drink selection is required when hasDrink = true");
            }
            Drink drink = drinkRepository.findById(req.getDrinkId())
                    .orElseThrow(() -> new RuntimeException("Drink not found: " + req.getDrinkId()));
            if (!drink.isAvailable()) {
                throw new IllegalStateException("Drink not available: " + drink.getName());
            }
            order.setDrinkName(drink.getName());
            order.setDrinkPrice(drink.getPrice());
        } else {
            order.setDrinkPrice(BigDecimal.ZERO);
        }

        order.calculateTotal();
        Order saved = orderRepository.save(order);
        log.info("Order #{} created by user {}", saved.getId(), username);
        return toResponse(saved);
    }

    public List<OrderResponse> getUserOrders(String username) {
        User user = userService.getUserByUsername(username);
        return orderRepository.findByUserOrderByOrderDateDesc(user)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> getUserOrdersByDate(String username, LocalDate date) {
        User user = userService.getUserByUsername(username);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);
        return orderRepository.findByUserAndDateRange(user, start, end)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> getPendingOrders() {
        return orderRepository.findByStatusOrderByOrderDateAsc(OrderStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public long getPendingCount() {
        return orderRepository.countByStatus(OrderStatus.PENDING);
    }

    @Transactional
    public OrderResponse confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order already processed: " + order.getStatus());
        }

        String cashierUsername = currentUsername();
        User cashier = userService.getUserByUsername(cashierUsername);
        if (!cashier.isCashier()) {
            throw new IllegalStateException("Only a cashier can confirm orders");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        order.setConfirmedBy(cashier);
        Order saved = orderRepository.save(order);

        emailService.sendOrderConfirmation(saved, saved.getUser());
        log.info("Order #{} confirmed by cashier {}", orderId, cashierUsername);

        return toResponse(saved);
    }

    @Transactional
    public void cancelOrder(Long orderId, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be cancelled");
        }
        if (!order.getUser().getUsername().equals(username)) {
            throw new IllegalStateException("You can only cancel your own orders");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order #{} cancelled by {}", orderId, username);
    }

    // ---- helpers ----

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new RuntimeException("No authenticated user");
        return auth.getName();
    }

    private OrderResponse toResponse(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .userId(o.getUser() != null ? o.getUser().getId() : null)
                .username(o.getUser() != null ? o.getUser().getUsername() : null)
                .userEmail(o.getUser() != null ? o.getUser().getEmail() : null)
                .mealType(o.getMealType())
                .mealName(o.getMealName())
                .lunchType(o.getLunchType())
                .amount(o.getAmount())
                .hasDrink(o.isHasDrink())
                .drinkPrice(o.getDrinkPrice())
                .drinkName(o.getDrinkName())
                .totalAmount(o.getTotalAmount())
                .status(o.getStatus())
                .orderDate(o.getOrderDate())
                .confirmedAt(o.getConfirmedAt())
                .confirmedByUsername(o.getConfirmedBy() != null ? o.getConfirmedBy().getUsername() : null)
                .notes(o.getNotes())
                .build();
    }
}