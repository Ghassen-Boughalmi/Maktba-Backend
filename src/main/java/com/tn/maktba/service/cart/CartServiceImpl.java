package com.tn.maktba.service.cart;

import com.tn.maktba.dto.cart.CartDTO;
import com.tn.maktba.dto.order.CartOrderDTO;
import com.tn.maktba.dto.order.ItemDTO;
import com.tn.maktba.model.cart.Cart;
import com.tn.maktba.model.cart.CartItem;
import com.tn.maktba.model.order.Order;
import com.tn.maktba.model.order.OrderItem;
import com.tn.maktba.model.order.OrderStatus;
import com.tn.maktba.model.product.Product;
import com.tn.maktba.model.user.UserEntity;
import com.tn.maktba.repository.CartRepository;
import com.tn.maktba.repository.OrderRepository;
import com.tn.maktba.repository.ProductRepository;
import com.tn.maktba.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ResponseEntity<?> addToCart(Long userId, Long productId, Integer quantity) {
        try {
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }

            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));

            if (product.getQuantity() < quantity) {
                throw new IllegalStateException("Requested quantity exceeds available stock: " + product.getName());
            }

            Cart cart = cartRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        Cart newCart = Cart.builder().user(user).items(new ArrayList<>()).build();
                        return cartRepository.save(newCart);
                    });

            if (cart.getItems() == null) {
                cart.setItems(new ArrayList<>());
            }

            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst();

            CartItem cartItem;
            if (existingItem.isPresent()) {
                cartItem = existingItem.get();
                int newQuantity = cartItem.getQuantity() + quantity;
                if (product.getQuantity() < newQuantity) {
                    throw new IllegalStateException("Requested quantity exceeds available stock: " + product.getName());
                }
                cartItem.setQuantity(newQuantity);
            } else {
                cartItem = CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(quantity)
                        .build();
                cart.getItems().add(cartItem);
            }

            cartRepository.save(cart);
            return ResponseEntity.ok(toCartItemDTO(cartItem));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to add to cart: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> removeFromCart(Long userId, Long productId) {
        try {
            Cart cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
            if (cart.getItems() == null) {
                cart.setItems(new ArrayList<>());
            }
            cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
            cartRepository.save(cart);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to remove from cart: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> resetCart(Long userId) {
        try {
            Cart cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
            if (cart.getItems() == null) {
                cart.setItems(new ArrayList<>());
            }
            cart.getItems().clear();
            cartRepository.save(cart);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to reset cart: " + e.getMessage()));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> confirmCart(Long userId) {
        try {
            Cart cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                throw new IllegalStateException("Cart is empty");
            }

            for (CartItem item : cart.getItems()) {
                Product product = item.getProduct();
                if (product.getQuantity() < item.getQuantity()) {
                    throw new IllegalStateException("Insufficient stock for product: " + product.getName());
                }
            }

            Order order = Order.builder()
                    .user(cart.getUser())
                    .items(cart.getItems().stream().map(item -> {
                        OrderItem orderItem = OrderItem.builder()
                                .product(item.getProduct())
                                .quantity(item.getQuantity())
                                .build();
                        return orderItem;
                    }).toList())
                    .totalPrice(calculateTotal(cart.getItems()))
                    .createdAt(LocalDateTime.now())
                    .status(OrderStatus.PENDING)
                    .build();

            order.getItems().forEach(item -> item.setOrder(order));
            orderRepository.save(order);
            cart.getItems().clear();
            cartRepository.save(cart);
            return ResponseEntity.ok(toCartOrderDTO(order));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to confirm cart: " + e.getMessage()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCartByUser(Long userId) {
        try {
            Cart cart = cartRepository.findByUserId(userId)
                    .orElseGet(() -> Cart.builder().user(UserEntity.builder().id(userId).build()).items(new ArrayList<>()).build());
            return ResponseEntity.ok(toCartDTO(cart));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve cart: " + e.getMessage()));
        }
    }

    private ItemDTO toCartItemDTO(CartItem cartItem) {
        return ItemDTO.builder()
                .productId(cartItem.getProduct().getId())
                .productName(cartItem.getProduct().getName())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getProduct().getPrice())
                .build();
    }

    private CartOrderDTO toCartOrderDTO(Order order) {
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

    private CartDTO toCartDTO(Cart cart) {
        return CartDTO.builder()
                .cartId(cart.getId())
                .userId(cart.getUser() != null ? cart.getUser().getId() : null)
                .items(cart.getItems() != null ? cart.getItems().stream().map(this::toCartItemDTO).toList() : new ArrayList<>())
                .build();
    }

    private Double calculateTotal(List<? extends Object> items) {
        return items.stream()
                .mapToDouble(item -> {
                    if (item instanceof CartItem cartItem) {
                        return cartItem.getProduct().getPrice() * cartItem.getQuantity();
                    } else if (item instanceof OrderItem orderItem) {
                        return orderItem.getProduct().getPrice() * orderItem.getQuantity();
                    }
                    return 0.0;
                })
                .sum();
    }
}