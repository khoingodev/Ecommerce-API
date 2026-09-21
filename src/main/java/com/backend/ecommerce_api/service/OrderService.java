package com.backend.ecommerce_api.service;

import com.backend.ecommerce_api.dto.OrderRequestDto;
import com.backend.ecommerce_api.dto.OrderResponseDto;
import com.backend.ecommerce_api.entity.Order;
import com.backend.ecommerce_api.entity.Product;
import com.backend.ecommerce_api.exception.InsufficientStockException;
import com.backend.ecommerce_api.exception.ProductNotFoundException;
import com.backend.ecommerce_api.repository.OrderRepository;
import com.backend.ecommerce_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String PENDING_STATUS = "PENDING";

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto request) {
        validateRequest(request);

        Product product = productRepository.findByIdForUpdate(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new InsufficientStockException();
        }

        product.setStockQuantity(product.getStockQuantity() - request.getQuantity());
        productRepository.save(product);

        Order order = Order.builder()
                .product(product)
                .quantity(request.getQuantity())
                .totalAmount(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())))
                .status(PENDING_STATUS)
                .build();

        return OrderResponseDto.from(orderRepository.save(order));
    }

    private void validateRequest(OrderRequestDto request) {
        if (request == null || request.getProductId() == null || request.getQuantity() == null
                || request.getProductId() <= 0 || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("productId and quantity must be positive values");
        }
    }
}
