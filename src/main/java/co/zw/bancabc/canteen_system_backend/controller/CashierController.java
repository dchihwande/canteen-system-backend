package co.zw.bancabc.canteen_system_backend.controller;

import co.zw.bancabc.canteen_system_backend.dto.ApiResponse;
import co.zw.bancabc.canteen_system_backend.dto.OrderResponse;
import co.zw.bancabc.canteen_system_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cashier")
@RequiredArgsConstructor
public class CashierController {

    private final OrderService orderService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> pending() {
        return ResponseEntity.ok(ApiResponse.success(orderService.getPendingOrders()));
    }

    @GetMapping("/pending-count")
    public ResponseEntity<ApiResponse<Long>> pendingCount() {
        return ResponseEntity.ok(ApiResponse.success(orderService.getPendingCount()));
    }

    @PostMapping("/confirm/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> confirm(@PathVariable Long orderId) {
        try {
            OrderResponse confirmed = orderService.confirmOrder(orderId);
            return ResponseEntity.ok(ApiResponse.success("Order confirmed", confirmed));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}