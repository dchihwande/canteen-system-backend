package co.zw.bancabc.canteen_system_backend.repository;

import co.zw.bancabc.canteen_system_backend.model.Order;
import co.zw.bancabc.canteen_system_backend.model.User;
import co.zw.bancabc.canteen_system_backend.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByOrderDateDesc(User user);

    List<Order> findByStatusOrderByOrderDateAsc(OrderStatus status);

    long countByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.orderDate BETWEEN :start AND :end ORDER BY o.orderDate DESC")
    List<Order> findByUserAndDateRange(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.status = co.zw.bancabc.canteen_system_backend.model.enums.OrderStatus.CONFIRMED " +
            "AND o.orderDate BETWEEN :start AND :end ORDER BY o.orderDate DESC")
    List<Order> findConfirmedOrdersBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}