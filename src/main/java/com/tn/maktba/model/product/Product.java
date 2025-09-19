package com.tn.maktba.model.product;

import com.tn.maktba.model.category.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String level;
    private Double price;
    private String publisher;
    private Integer quantity;
    private String imageURL;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}