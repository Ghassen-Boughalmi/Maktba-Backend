package com.tn.maktba.controller.order;

import com.tn.maktba.dto.order.OrderRequests;
import com.tn.maktba.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PutMapping("/modify/{orderId}")
    public ResponseEntity<?> modifyOrder(@PathVariable Long orderId, @RequestBody OrderRequests.ModifyOrderRequest request) {
        return orderService.modifyOrder(request.userId(), orderId, request.updates());
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<?> getAdminOrders() {
        return orderService.getAdminOrders();
    }

    @PutMapping("/admin/prepare/{orderId}")
    public ResponseEntity<?> prepareOrder(@PathVariable Long orderId) {
        return orderService.prepareOrder(orderId);
    }

    @DeleteMapping("/admin/remove/{orderId}")
    public ResponseEntity<?> removeOrder(@PathVariable Long orderId) {
        return orderService.removeOrder(orderId);
    }
}
