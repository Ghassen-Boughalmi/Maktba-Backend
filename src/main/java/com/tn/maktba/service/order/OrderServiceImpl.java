package com.tn.maktba.service.order;

import com.tn.maktba.dto.order.CartOrderDTO;
import com.tn.maktba.dto.order.ItemDTO;
import com.tn.maktba.model.order.Order;
import com.tn.maktba.model.order.OrderItem;
import com.tn.maktba.model.order.OrderStatus;
import com.tn.maktba.model.product.Product;
import com.tn.maktba.repository.OrderRepository;
import com.tn.maktba.repository.ProductRepository;
import com.tn.maktba.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ResponseEntity<?> modifyOrder(Long userId, Long orderId, Map<Long, Integer> updates) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException("Order not found"));
            if (!order.getUser().getId().equals(userId) || order.getStatus() == OrderStatus.PROCESSED) {
                throw new IllegalStateException("Cannot modify order");
            }

            order.getItems().removeIf(item -> !updates.containsKey(item.getProduct().getId()));
            for (Map.Entry<Long, Integer> entry : updates.entrySet()) {
                Long productId = entry.getKey();
                Integer quantity = entry.getValue();
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new EntityNotFoundException("Product not found"));

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
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to modify order"));
        }
    }

    @Override
    public ResponseEntity<?> getAdminOrders() {
        try {
            List<CartOrderDTO> orders = orderRepository.findByStatusIn(List.of(OrderStatus.PENDING, OrderStatus.MODIFIED))
                    .stream()
                    .map(this::toDTO)
                    .toList();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve orders"));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> prepareOrder(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException("Order not found"));
            if (order.getStatus() == OrderStatus.PROCESSED) {
                throw new IllegalStateException("Order already processed");
            }

            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                int newQuantity = product.getQuantity() - item.getQuantity();
                if (newQuantity < 0) {
                    throw new IllegalStateException("Insufficient stock for product: " + product.getName());
                }
                product.setQuantity(newQuantity);
                productRepository.save(product);
            }

            order.setStatus(OrderStatus.PROCESSED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            return ResponseEntity.ok(toDTO(order));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to prepare order"));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> removeOrder(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException("Order not found"));
            if (order.getStatus() == OrderStatus.PROCESSED) {
                throw new IllegalStateException("Cannot remove processed order");
            }

            orderRepository.delete(order);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to remove order"));
        }
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
