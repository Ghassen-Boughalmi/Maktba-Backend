package com.tn.maktba.controller.product;


import com.tn.maktba.dto.product.ProductRequestDTO;
import com.tn.maktba.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/create")
    public Optional<?> createProduct(@ModelAttribute ProductRequestDTO productDTO) throws IOException {
        return Optional.of(productService.createProduct(productDTO));
    }

    @GetMapping("/get/{id}")
    public Optional<?> getProduct(@PathVariable Long id) {
        return Optional.of(productService.getProduct(id));
    }

    @GetMapping("/get-all")
    public Optional<?> getAllProducts() {
        return Optional.of(productService.getAllProducts());
    }

    @PutMapping("/update/{id}")
    public Optional<?> updateProduct(@PathVariable Long id, @ModelAttribute ProductRequestDTO productDTO) throws IOException {
        return Optional.of(productService.updateProduct(id, productDTO));
    }

    @DeleteMapping("/delete/{id}")
    public Optional<?> deleteProduct(@PathVariable Long id) throws IOException {
        productService.deleteProduct(id);
        return Optional.empty();
    }
}
