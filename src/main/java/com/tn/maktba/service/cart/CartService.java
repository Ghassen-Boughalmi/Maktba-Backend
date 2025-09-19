package com.tn.maktba.service.cart;

import org.springframework.http.ResponseEntity;

public interface CartService {
    ResponseEntity<?> addToCart(Long userId, Long productId, Integer quantity);
    ResponseEntity<?> removeFromCart(Long userId, Long productId);
    ResponseEntity<?> resetCart(Long userId);
    ResponseEntity<?> confirmCart(Long userId);
    ResponseEntity<?> getCartByUser(Long userId);
}