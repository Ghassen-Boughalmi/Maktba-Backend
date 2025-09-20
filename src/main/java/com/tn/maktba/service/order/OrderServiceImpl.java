package com.tn.maktba.service.order;

import com.tn.maktba.dto.order.CartOrderDTO;
import com.tn.maktba.dto.order.ItemDTO;
import com.tn.maktba.model.order.Order;
import com.tn.maktba.model.order.OrderItem;
import com.tn.maktba.model.order.OrderStatus;
import com.tn.maktba.model.product.Product;
import com.tn.maktba.repository.OrderRepository;
import com.tn.maktba.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;

        this.productRepository = productRepository;
    }

    @Override
    public ResponseEntity<?> modifyOrder(Long userId, Long orderId, Map<Long, Integer> updates) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Order not found"));
        }
        if (!order.getUser().getId().equals(userId) || order.getStatus() == OrderStatus.PROCESSED) {
            return ResponseEntity.status(400).body(Map.of("error", "Cannot modify order"));
        }

        order.getItems().removeIf(item -> !updates.containsKey(item.getProduct().getId()));
        for (Map.Entry<Long, Integer> entry : updates.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Product not found"));
            }

            Optional<OrderItem> existingItem = order.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst();

            if (existingItem.isPresent()) {
                existingItem.get().setQuantity(quantity);
            } else {
                OrderItem newItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .quantity(quantity)
                        .build();
                order.getItems().add(newItem);
            }
        }

        order.setTotalPrice(calculateTotal(order.getItems()));
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.MODIFIED);
        orderRepository.save(order);
        return ResponseEntity.ok(toDTO(order));
    }

    @Override
    public ResponseEntity<?> getAdminOrders() {
        List<CartOrderDTO> orders = orderRepository.findByStatusIn(List.of(OrderStatus.PENDING, OrderStatus.MODIFIED))
                .stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @Override
    public ResponseEntity<?> prepareOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Order not found"));
        }
        if (order.getStatus() == OrderStatus.PROCESSED) {
            return ResponseEntity.status(400).body(Map.of("error", "Order already processed"));
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            int newQuantity = product.getQuantity() - item.getQuantity();
            if (newQuantity < 0) {
                return ResponseEntity.status(400).body(Map.of("error", "Insufficient stock for product: " + product.getName()));
            }
            product.setQuantity(newQuantity);
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.PROCESSED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        return ResponseEntity.ok(toDTO(order));
    }

    @Override
    public ResponseEntity<?> removeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Order not found"));
        }
        if (order.getStatus() == OrderStatus.PROCESSED) {
            return ResponseEntity.status(400).body(Map.of("error", "Cannot remove processed order"));
        }

        orderRepository.delete(order);
        return ResponseEntity.ok().build();
    }

    private CartOrderDTO toDTO(Order order) {
        return CartOrderDTO.builder()
                .orderId(order.getId())
                .userId(order.getUser().getId())
                .items(order.getItems().stream().map(item -> ItemDTO.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getProduct().getPrice())
                        .build()).toList())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .status(order.getStatus())
                .build();
    }

    private Double calculateTotal(List<? extends Object> items) {
        return items.stream()
                .mapToDouble(item -> {
                    if (item instanceof OrderItem orderItem) {
                        return orderItem.getProduct().getPrice() * orderItem.getQuantity();
                    }
                    return 0.0;
                })
                .sum();
    }
}