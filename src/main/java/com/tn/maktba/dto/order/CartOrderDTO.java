package com.tn.maktba.dto.order;

import com.tn.maktba.model.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartOrderDTO {
    private Long orderId;
    private Long userId;
    private List<ItemDTO> items;
    private Double totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private OrderStatus status;
}


