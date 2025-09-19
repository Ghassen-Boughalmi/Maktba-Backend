package com.tn.maktba.service.order;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface OrderService {
    ResponseEntity<?> modifyOrder(Long userId, Long orderId, Map<Long, Integer> updates);
    ResponseEntity<?> getAdminOrders();
    ResponseEntity<?> prepareOrder(Long orderId);
    ResponseEntity<?> removeOrder(Long orderId);
}
