package co.zw.bancabc.canteen_system_backend.dto;

import co.zw.bancabc.canteen_system_backend.model.enums.LunchType;
import co.zw.bancabc.canteen_system_backend.model.enums.MealType;
import co.zw.bancabc.canteen_system_backend.model.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private String username;
    private String userEmail;
    private MealType mealType;
    private String mealName;
    private LunchType lunchType;
    private BigDecimal amount;
    private boolean hasDrink;
    private BigDecimal drinkPrice;
    private String drinkName;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private LocalDateTime confirmedAt;
    private String confirmedByUsername;
    private String notes;
}