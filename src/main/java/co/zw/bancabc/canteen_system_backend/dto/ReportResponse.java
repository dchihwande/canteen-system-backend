package co.zw.bancabc.canteen_system_backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {
    private Long orderId;
    private String username;
    private String userEmail;
    private String mealName;
    private BigDecimal amount;
    private boolean hasDrink;
    private BigDecimal drinkPrice;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private LocalDateTime confirmedAt;
}