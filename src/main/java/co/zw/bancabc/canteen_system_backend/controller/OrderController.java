package co.zw.bancabc.canteen_system_backend.controller;

import co.zw.bancabc.canteen_system_backend.dto.ApiResponse;
import co.zw.bancabc.canteen_system_backend.dto.OrderRequest;
import co.zw.bancabc.canteen_system_backend.dto.OrderResponse;
import co.zw.bancabc.canteen_system_backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(@Valid @RequestBody OrderRequest req) {
        try {
            OrderResponse created = orderService.createOrder(req);
            return ResponseEntity.ok(ApiResponse.success("Order placed successfully", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> myOrders(Authentication auth) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    orderService.getUserOrders(auth.getName())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/date")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> myOrdersByDate(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    orderService.getUserOrdersByDate(auth.getName(), date)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> cancel(Authentication auth, @PathVariable Long orderId) {
        try {
            orderService.cancelOrder(orderId, auth.getName());
            return ResponseEntity.ok(ApiResponse.success("Order cancelled", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}