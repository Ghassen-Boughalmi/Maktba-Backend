package com.tn.maktba.service.product;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.tn.maktba.dto.product.ProductDTO;
import com.tn.maktba.dto.product.ProductRequestDTO;
import com.tn.maktba.model.category.Category;
import com.tn.maktba.model.product.Product;
import com.tn.maktba.repository.CategoryRepository;
import com.tn.maktba.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final Cloudinary cloudinary;

    @Override
    public ProductDTO createProduct(ProductRequestDTO productRequestDTO) throws IOException {
        System.out.println("Received ProductRequestDTO: " + productRequestDTO);
        Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

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
        return convertToDTO(product);
    }

    @Override
    public ProductDTO getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return convertToDTO(product);
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductRequestDTO productRequestDTO) throws IOException {
        System.out.println("Received ProductRequestDTO: " + productRequestDTO);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

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
        return convertToDTO(product);
    }

    @Override
    public void deleteProduct(Long id) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.getImageURL() != null) {
            String publicId = extractPublicId(product.getImageURL());
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
        productRepository.deleteById(id);
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