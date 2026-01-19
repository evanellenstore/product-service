package com.store.product.service;

import com.store.product.dto.CategoryRequest;
import com.store.product.dto.CategoryResponse;
import com.store.product.entity.Category;
import com.store.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryResponse create(CategoryRequest request) {

        if (categoryRepository.existsByNameIgnoreCase(request.getCategory())) {
            throw new RuntimeException("Category already exists");
        }

        Category category = Category.builder()
                .name(request.getCategory())
                .build();

        categoryRepository.save(category);

        return new CategoryResponse(category.getId(), category.getName());
    }

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
    }
}
