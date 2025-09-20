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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartServiceImpl(CartRepository cartRepository, OrderRepository orderRepository,
                           UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    public ResponseEntity<?> addToCart(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            return ResponseEntity.status(400).body(Map.of("error", "Quantity must be greater than zero"));
        }

        UserEntity user = userRepository.findById(userId)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        Product product = productRepository.findById(productId)
                .orElse(null);
        if (product == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Product not found"));
        }

        if (product.getQuantity() < quantity) {
            return ResponseEntity.status(400).body(Map.of("error", "Requested quantity exceeds available stock: " + product.getName()));
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
                return ResponseEntity.status(400).body(Map.of("error", "Requested quantity exceeds available stock: " + product.getName()));
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
    }

    @Override
    public ResponseEntity<?> removeFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(null);
        if (cart == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Cart not found"));
        }

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }
        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        cartRepository.save(cart);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> resetCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(null);
        if (cart == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Cart not found"));
        }

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }
        cart.getItems().clear();
        cartRepository.save(cart);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> confirmCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(null);
        if (cart == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Cart not found"));
        }

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("error", "Cart is empty"));
        }

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.getQuantity() < item.getQuantity()) {
                return ResponseEntity.status(400).body(Map.of("error", "Insufficient stock for product: " + product.getName()));
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
    }

    @Override
    public ResponseEntity<?> getCartByUser(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.builder().user(UserEntity.builder().id(userId).build()).items(new ArrayList<>()).build());
        return ResponseEntity.ok(toCartDTO(cart));
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