package com.backend.ecommerce_api.service;

import com.backend.ecommerce_api.dto.ProductRequestDto;
import com.backend.ecommerce_api.dto.ProductResponseDto;
import com.backend.ecommerce_api.entity.Product;
import com.backend.ecommerce_api.exception.ProductNotFoundException;
import com.backend.ecommerce_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductResponseDto> findAll() {
        return productRepository.findAll().stream()
                .map(ProductResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponseDto findById(Long id) {
        return ProductResponseDto.from(getProduct(id));
    }

    @Transactional
    public ProductResponseDto create(ProductRequestDto request) {
        Product product = Product.builder()
                .name(request.getName().trim())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .build();
        return ProductResponseDto.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponseDto update(Long id, ProductRequestDto request) {
        Product product = getProduct(id);
        product.setName(request.getName().trim());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        return ProductResponseDto.from(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = getProduct(id);
        productRepository.delete(product);
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
