package com.tn.maktba.repository;

import com.tn.maktba.model.order.Order;
import com.tn.maktba.model.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);
    List<Order> findByStatusIn(List<OrderStatus> statuses);
}