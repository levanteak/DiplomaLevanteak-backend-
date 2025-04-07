package com.diplom.levanteak.service.interf;

import com.diplom.levanteak.dto.CategoryDto;
import com.diplom.levanteak.dto.Response;

public interface CategoryService {

    Response createCategory(CategoryDto categoryRequest);
    Response updateCategory(Long categoryId, CategoryDto categoryRequest);
    Response getAllCategories();
    Response getCategoryById(Long categoryId);
    Response deleteCategory(Long categoryId);
}
