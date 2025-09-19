package com.tn.maktba.dto.product;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String level;
    private Double price;
    private String publisher;
    private Integer quantity;
    private Long categoryId;
    private String imageURL;
}
