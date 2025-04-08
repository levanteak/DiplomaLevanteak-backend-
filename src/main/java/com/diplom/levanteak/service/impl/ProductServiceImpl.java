package com.diplom.levanteak.service.impl;

import com.diplom.levanteak.exception.NotFoundException;
import com.diplom.levanteak.service.AwsS3Service;
import com.diplom.levanteak.dto.ProductDto;
import com.diplom.levanteak.dto.Response;
import com.diplom.levanteak.entity.Category;
import com.diplom.levanteak.entity.Product;
import com.diplom.levanteak.mapper.EntityDtoMapper;
import com.diplom.levanteak.repository.CategoryRepo;
import com.diplom.levanteak.repository.ProductRepo;
import com.diplom.levanteak.service.interf.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final EntityDtoMapper entityDtoMapper;
    private final AwsS3Service awsS3Service;



    @Override
    public Response createProduct(Long categoryId, MultipartFile image, String name, String description, BigDecimal price) {
        log.info("Creating product: {}", name);

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category not found with ID: {}", categoryId);
                    return new NotFoundException("Category not found");
                });
        String productImageUrl = awsS3Service.saveImageToS3(image);
        log.debug("Image uploaded to S3: {}", productImageUrl);


        Product product = new Product();
        product.setCategory(category);
        product.setPrice(price);
        product.setName(name);
        product.setDescription(description);
        product.setImageUrl(productImageUrl);

        productRepo.save(product);
        log.info("Product '{}' successfully created with ID: {}", name, product.getId());

        return Response.builder()
                .status(200)
                .message("Product successfully created")
                .build();
    }

    @Override
    public Response updateProduct(Long productId, Long categoryId, MultipartFile image, String name, String description, BigDecimal price) {
        log.info("Updating product with ID: {}", productId);

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", productId);
                    return new NotFoundException("Product Not Found");
                });

        Category category = null;
        String productImageUrl = null;

        if (categoryId != null) {
            category = categoryRepo.findById(categoryId)
                    .orElseThrow(() -> {
                        log.warn("Category not found with ID: {}", categoryId);
                        return new NotFoundException("Category not found");
                    });
        }
        if (image != null && !image.isEmpty()) {
            productImageUrl = awsS3Service.saveImageToS3(image);
            log.debug("Updated product image uploaded to S3: {}", productImageUrl);
        }

        if (category != null) product.setCategory(category);
        if (name != null) product.setName(name);
        if (price != null) product.setPrice(price);
        if (description != null) product.setDescription(description);
        if (productImageUrl != null) product.setImageUrl(productImageUrl);

        productRepo.save(product);
        log.info("Product with ID {} updated successfully", productId);

        return Response.builder()
                .status(200)
                .message("Product updated successfully")
                .build();

    }

    @Override
    public Response deleteProduct(Long productId) {
        log.info("Deleting product with ID: {}", productId);
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", productId);
                    return new NotFoundException("Product Not Found");
                });

        productRepo.delete(product);
        log.info("Product with ID {} deleted successfully", productId);


        return Response.builder()
                .status(200)
                .message("Product deleted successfully")
                .build();
    }

    @Override
    public Response getProductById(Long productId) {
        log.info("Fetching product with ID: {}", productId);

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", productId);
                    return new NotFoundException("Product Not Found");
                });

        ProductDto productDto = entityDtoMapper.mapProductToDtoBasic(product);
        log.debug("Fetched product: {}", productDto.getName());


        return Response.builder()
                .status(200)
                .product(productDto)
                .build();
    }

    @Override
    public Response getAllProducts() {
        log.info("Fetching all products");

        List<ProductDto> productList = productRepo.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());

        log.debug("Total products found: {}", productList.size());

        return Response.builder()
                .status(200)
                .productList(productList)
                .build();

    }

    @Override
    public Response getProductsByCategory(Long categoryId) {
        log.info("Fetching products by category ID: {}", categoryId);

        List<Product> products = productRepo.findByCategoryId(categoryId);
        if (products.isEmpty()) {
            log.warn("No products found for category ID: {}", categoryId);
            throw new NotFoundException("No Products found for this category");
        }
        List<ProductDto> productDtoList = products.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());
        log.debug("Products found for category {}: {}", categoryId, productDtoList.size());


        return Response.builder()
                .status(200)
                .productList(productDtoList)
                .build();

    }

    @Override
    public Response searchProduct(String searchValue) {
        log.info("Searching products by value: {}", searchValue);
        List<Product> products = productRepo.findByNameContainingOrDescriptionContaining(searchValue, searchValue);

        if (products.isEmpty()) {
            log.warn("No products found for search value: {}", searchValue);
            throw new NotFoundException("No Products Found");
        }

        List<ProductDto> productDtoList = products.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());

        log.debug("Products found for search '{}': {}", searchValue, productDtoList.size());

        return Response.builder()
                .status(200)
                .productList(productDtoList)
                .build();
    }
}
