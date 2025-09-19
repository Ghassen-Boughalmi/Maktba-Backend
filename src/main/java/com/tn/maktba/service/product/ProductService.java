package com.tn.maktba.service.product;

import com.tn.maktba.dto.product.ProductDTO;
import com.tn.maktba.dto.product.ProductRequestDTO;

import java.io.IOException;
import java.util.List;

public interface ProductService {
    ProductDTO createProduct(ProductRequestDTO productRequestDTO) throws IOException;
    ProductDTO getProduct(Long id);
    List<ProductDTO> getAllProducts();
    ProductDTO updateProduct(Long id, ProductRequestDTO productRequestDTO) throws IOException;
    void deleteProduct(Long id) throws IOException;
}
