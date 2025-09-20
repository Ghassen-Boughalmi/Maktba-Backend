package com.tn.maktba.service.product;

import com.tn.maktba.dto.product.ProductRequestDTO;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface ProductService {
    ResponseEntity<?> createProduct(ProductRequestDTO productRequestDTO) throws IOException;
    ResponseEntity<?> getProduct(Long id);
    ResponseEntity<?> getAllProducts();
    ResponseEntity<?> updateProduct(Long id, ProductRequestDTO productRequestDTO) throws IOException;
    ResponseEntity<?> deleteProduct(Long id) throws IOException;
}