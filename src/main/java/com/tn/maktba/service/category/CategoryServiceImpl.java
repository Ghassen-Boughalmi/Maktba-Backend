package com.tn.maktba.service.category;

import com.tn.maktba.dto.category.CategoryDTO;
import com.tn.maktba.dto.category.CategoryRequestDTO;
import com.tn.maktba.model.category.Category;
import com.tn.maktba.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ResponseEntity<?> createCategory(CategoryRequestDTO categoryRequestDTO) {
        Category category = new Category();
        category.setName(categoryRequestDTO.getName());
        category = categoryRepository.save(category);
        return ResponseEntity.ok(convertToDTO(category));
    }

    @Override
    public ResponseEntity<?> getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElse(null);
        if (category == null) {
            return ResponseEntity.status(404).body("Category not found");
        }
        return ResponseEntity.ok(convertToDTO(category));
    }

    @Override
    public ResponseEntity<?> getAllCategories() {
        List<CategoryDTO> categories = categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @Override
    public ResponseEntity<?> updateCategory(Long id, CategoryRequestDTO categoryRequestDTO) {
        Category category = categoryRepository.findById(id)
                .orElse(null);
        if (category == null) {
            return ResponseEntity.status(404).body("Category not found");
        }
        category.setName(categoryRequestDTO.getName());
        category = categoryRepository.save(category);
        return ResponseEntity.ok(convertToDTO(category));
    }

    @Override
    public ResponseEntity<?> deleteCategory(Long id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}