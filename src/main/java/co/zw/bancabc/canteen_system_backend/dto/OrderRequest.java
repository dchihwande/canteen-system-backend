package co.zw.bancabc.canteen_system_backend.dto;

import co.zw.bancabc.canteen_system_backend.model.enums.LunchType;
import co.zw.bancabc.canteen_system_backend.model.enums.MealType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {

    @NotNull(message = "Meal type is required")
    private MealType mealType;

    private LunchType lunchType;

    /** Only used for BREAKFAST orders */
    private BigDecimal amount;

    private Boolean hasDrink = false;

    private Long drinkId;

    private String notes;
}