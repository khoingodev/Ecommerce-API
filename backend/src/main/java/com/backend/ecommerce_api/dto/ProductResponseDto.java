package com.backend.ecommerce_api.dto;

import com.backend.ecommerce_api.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ProductResponseDto {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;

    public static ProductResponseDto from(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }
}
