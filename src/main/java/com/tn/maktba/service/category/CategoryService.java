package com.tn.maktba.service.category;

import com.tn.maktba.dto.category.CategoryRequestDTO;
import org.springframework.http.ResponseEntity;

public interface CategoryService {
    ResponseEntity<?> createCategory(CategoryRequestDTO categoryRequestDTO);
    ResponseEntity<?> getCategory(Long id);
    ResponseEntity<?> getAllCategories();
    ResponseEntity<?> updateCategory(Long id, CategoryRequestDTO categoryRequestDTO);
    ResponseEntity<?> deleteCategory(Long id);
}