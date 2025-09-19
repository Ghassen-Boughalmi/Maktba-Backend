package com.tn.maktba.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDTO {
    private Long id;
    private Long cartId;
    private Long userId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
}