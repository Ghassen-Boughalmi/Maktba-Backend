package com.tn.maktba.controller.category;


import com.tn.maktba.dto.category.CategoryRequestDTO;
import com.tn.maktba.service.category.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/create")
    public Optional<?> createCategory(@RequestBody CategoryRequestDTO categoryDTO) {
        return Optional.of(categoryService.createCategory(categoryDTO));
    }

    @GetMapping("/get/{id}")
    public Optional<?> getCategory(@PathVariable Long id) {
        return Optional.of(categoryService.getCategory(id));
    }

    @GetMapping("/get-all")
    public Optional<?> getAllCategories() {
        return Optional.of(categoryService.getAllCategories());
    }

    @PutMapping("/update/{id}")
    public Optional<?> updateCategory(@PathVariable Long id, @RequestBody CategoryRequestDTO categoryDTO) {
        return Optional.of(categoryService.updateCategory(id, categoryDTO));
    }

    @DeleteMapping("/delete/{id}")
    public Optional<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Optional.empty();
    }
}
