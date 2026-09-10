package co.zw.bancabc.canteen_system_backend.model;

import co.zw.bancabc.canteen_system_backend.model.enums.LunchType;
import co.zw.bancabc.canteen_system_backend.model.enums.MealType;
import co.zw.bancabc.canteen_system_backend.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user", columnList = "user_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_order_date", columnList = "order_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 20)
    private MealType mealType;

    @Column(name = "meal_name", length = 100)
    private String mealName;

    @Enumerated(EnumType.STRING)
    @Column(name = "lunch_type", length = 20)
    private LunchType lunchType;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "has_drink")
    private boolean hasDrink = false;

    @Column(name = "drink_price", precision = 10, scale = 2)
    private BigDecimal drinkPrice = BigDecimal.ZERO;

    @Column(name = "drink_name", length = 100)
    private String drinkName;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @CreationTimestamp
    @Column(name = "order_date", updatable = false)
    private LocalDateTime orderDate;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by")
    private User confirmedBy;

    @Column(length = 500)
    private String notes;

    public BigDecimal calculateTotal() {
        BigDecimal total = amount != null ? amount : BigDecimal.ZERO;
        if (hasDrink && drinkPrice != null) {
            total = total.add(drinkPrice);
        }
        this.totalAmount = total;
        return total;
    }
}