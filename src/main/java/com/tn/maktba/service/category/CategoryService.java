package com.tn.maktba.service.category;

import com.tn.maktba.dto.category.CategoryDTO;
import com.tn.maktba.dto.category.CategoryRequestDTO;

import java.util.List;

public interface CategoryService {
    CategoryDTO createCategory(CategoryRequestDTO categoryRequestDTO);
    CategoryDTO getCategory(Long id);
    List<CategoryDTO> getAllCategories();
    CategoryDTO updateCategory(Long id, CategoryRequestDTO categoryRequestDTO);
    void deleteCategory(Long id);
}