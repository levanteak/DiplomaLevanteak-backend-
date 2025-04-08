package com.diplom.levanteak.service.impl;

import com.diplom.levanteak.dto.CategoryDto;
import com.diplom.levanteak.dto.Response;
import com.diplom.levanteak.entity.Category;
import com.diplom.levanteak.exception.NotFoundException;
import com.diplom.levanteak.repository.CategoryRepo;
import com.diplom.levanteak.service.interf.CategoryService;
import com.diplom.levanteak.mapper.EntityDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepo categoryRepo;
    private final EntityDtoMapper entityDtoMapper;




    @Override
    public Response createCategory(CategoryDto categoryRequest) {
        log.info("Creating new category with name: {}", categoryRequest.getName());
        Category category = new Category();
        category.setName(categoryRequest.getName());
        categoryRepo.save(category);
        log.info("Category created successfully with ID: {}", category.getId());

        return Response.builder()
                .status(200)
                .message("Category created successfully")
                .build();
    }

    @Override
    public Response updateCategory(Long categoryId, CategoryDto categoryRequest) {
        log.info("Updating category with ID: {}", categoryId);

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category with ID {} not found", categoryId);
                    return new NotFoundException("Category Not Found");
                });
        log.debug("Existing category name: {}", category.getName());

        category.setName(categoryRequest.getName());
        categoryRepo.save(category);
        log.info("Category updated successfully. New name: {}", category.getName());
        return Response.builder()
                .status(200)
                .message("category updated successfully")
                .build();
    }

    @Override
    public Response getAllCategories() {
        log.info("Fetching all categories");
        List<Category> categories = categoryRepo.findAll();
        log.debug("Found {} categories", categories.size());
        List<CategoryDto> categoryDtoList = categories.stream()
                .map(entityDtoMapper::mapCategoryToDtoBasic)
                .collect(Collectors.toList());

        return  Response.builder()
                .status(200)
                .categoryList(categoryDtoList)
                .build();
    }

    @Override
    public Response getCategoryById(Long categoryId) {
        log.info("Fetching category by ID: {}", categoryId);

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category with ID {} not found", categoryId);
                    return new NotFoundException("Category Not Found");
                });


        CategoryDto categoryDto = entityDtoMapper.mapCategoryToDtoBasic(category);
        log.debug("Category found: {}", categoryDto.getName());
        return Response.builder()
                .status(200)
                .category(categoryDto)
                .build();
    }

    @Override
    public Response deleteCategory(Long categoryId) {
        log.info("Deleting category with ID: {}", categoryId);

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category with ID {} not found", categoryId);
                    return new NotFoundException("Category Not Found");
                });

        categoryRepo.delete(category);
        log.info("Category with ID {} deleted successfully", categoryId);

        return Response.builder()
                .status(200)
                .message("Category was deleted successfully")
                .build();
    }
}
