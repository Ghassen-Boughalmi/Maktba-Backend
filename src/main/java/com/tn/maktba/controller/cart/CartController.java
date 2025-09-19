package com.tn.maktba.controller.cart;

import com.tn.maktba.dto.cart.CartRequests;
import com.tn.maktba.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartRequests.AddToCartRequest request) {
        return cartService.addToCart(request.userId(), request.productId(), request.quantity());
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestBody CartRequests.RemoveFromCartRequest request) {
        return cartService.removeFromCart(request.userId(), request.productId());
    }

    @DeleteMapping("/reset/{userId}")
    public ResponseEntity<?> resetCart(@PathVariable Long userId) {
        return cartService.resetCart(userId);
    }

    @PostMapping("/confirm/{userId}")
    public ResponseEntity<?> confirmCart(@PathVariable Long userId) {
        return cartService.confirmCart(userId);
    }

    @GetMapping("/get/{userId}")
    public ResponseEntity<?> getCartByUser(@PathVariable Long userId) {
        return cartService.getCartByUser(userId);
    }
}