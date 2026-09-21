package com.backend.ecommerce_api.dto;

import com.backend.ecommerce_api.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class OrderResponseDto {

    private Long orderId;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalAmount;
    private String status;

    public static OrderResponseDto from(Order order) {
        return OrderResponseDto.builder()
                .orderId(order.getId())
                .productId(order.getProduct().getId())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .build();
    }
}
