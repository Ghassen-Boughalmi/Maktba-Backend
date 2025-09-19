package com.tn.maktba.dto.cart;

public record CartRequests() {
    public record AddToCartRequest(Long userId, Long productId, Integer quantity) {}
    public record RemoveFromCartRequest(Long userId, Long productId) {}
}