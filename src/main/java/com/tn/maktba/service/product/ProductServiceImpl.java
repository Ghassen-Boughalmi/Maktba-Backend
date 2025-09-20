package com.tn.maktba.service.product;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.tn.maktba.dto.product.ProductDTO;
import com.tn.maktba.dto.product.ProductRequestDTO;
import com.tn.maktba.model.category.Category;
import com.tn.maktba.model.product.Product;
import com.tn.maktba.repository.CategoryRepository;
import com.tn.maktba.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final Cloudinary cloudinary;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, Cloudinary cloudinary) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cloudinary = cloudinary;
    }

    @Override
    public ResponseEntity<?> createProduct(ProductRequestDTO productRequestDTO) throws IOException {
        Category category = categoryRepository.findById(productRequestDTO.getCategoryId()).orElse(null);
        if (category == null) {
            return ResponseEntity.status(404).body("Category not found");
        }

        String imageURL = null;
        if (productRequestDTO.getImage() != null && !productRequestDTO.getImage().isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(productRequestDTO.getImage().getBytes(),
                    ObjectUtils.asMap("resource_type", "image"));
            imageURL = uploadResult.get("secure_url").toString();
        }

        Product product = new Product();
        product.setName(productRequestDTO.getName());
        product.setDescription(productRequestDTO.getDescription());
        product.setLevel(productRequestDTO.getLevel());
        product.setPrice(productRequestDTO.getPrice());
        product.setPublisher(productRequestDTO.getPublisher());
        product.setQuantity(productRequestDTO.getQuantity());
        product.setImageURL(imageURL);
        product.setCategory(category);
        product = productRepository.save(product);
        return ResponseEntity.ok(convertToDTO(product));
    }

    @Override
    public ResponseEntity<?> getProduct(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return ResponseEntity.status(404).body("Product not found");
        }
        return ResponseEntity.ok(convertToDTO(product));
    }

    @Override
    public ResponseEntity<?> getAllProducts() {
        List<ProductDTO> products = productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @Override
    public ResponseEntity<?> updateProduct(Long id, ProductRequestDTO productRequestDTO) throws IOException {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return ResponseEntity.status(404).body("Product not found");
        }
        Category category = categoryRepository.findById(productRequestDTO.getCategoryId()).orElse(null);
        if (category == null) {
            return ResponseEntity.status(404).body("Category not found");
        }

        if (productRequestDTO.getImage() != null && !productRequestDTO.getImage().isEmpty()) {
            if (product.getImageURL() != null) {
                String publicId = extractPublicId(product.getImageURL());
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
            Map uploadResult = cloudinary.uploader().upload(productRequestDTO.getImage().getBytes(),
                    ObjectUtils.asMap("resource_type", "image"));
            product.setImageURL(uploadResult.get("secure_url").toString());
        }

        product.setName(productRequestDTO.getName());
        product.setDescription(productRequestDTO.getDescription());
        product.setLevel(productRequestDTO.getLevel());
        product.setPrice(productRequestDTO.getPrice());
        product.setPublisher(productRequestDTO.getPublisher());
        product.setQuantity(productRequestDTO.getQuantity());
        product.setCategory(category);
        product = productRepository.save(product);
        return ResponseEntity.ok(convertToDTO(product));
    }

    @Override
    public ResponseEntity<?> deleteProduct(Long id) throws IOException {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return ResponseEntity.status(404).body("Product not found");
        }
        if (product.getImageURL() != null) {
            String publicId = extractPublicId(product.getImageURL());
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setLevel(product.getLevel());
        dto.setPrice(product.getPrice());
        dto.setPublisher(product.getPublisher());
        dto.setQuantity(product.getQuantity());
        dto.setImageURL(product.getImageURL());
        dto.setCategoryId(product.getCategory().getId());
        return dto;
    }

    private String extractPublicId(String imageURL) {
        String[] parts = imageURL.split("/");
        String fileName = parts[parts.length - 1];
        return fileName.split("\\.")[0];
    }
}