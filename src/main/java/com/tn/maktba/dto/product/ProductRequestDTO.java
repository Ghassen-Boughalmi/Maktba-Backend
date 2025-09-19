package com.tn.maktba.dto.product;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {
    private String name;
    private String description;
    private String level;
    private Double price;
    private String publisher;
    private Integer quantity;
    private Long categoryId;
    private MultipartFile image;
}
