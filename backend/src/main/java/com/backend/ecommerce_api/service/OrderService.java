package com.backend.ecommerce_api.service;

import com.backend.ecommerce_api.dto.OrderRequestDto;
import com.backend.ecommerce_api.dto.OrderResponseDto;
import com.backend.ecommerce_api.dto.AdminOrderResponseDto;
import com.backend.ecommerce_api.exception.OrderNotFoundException;
import com.backend.ecommerce_api.entity.Order;
import com.backend.ecommerce_api.entity.Product;
import com.backend.ecommerce_api.entity.User;
import com.backend.ecommerce_api.exception.InsufficientStockException;
import com.backend.ecommerce_api.exception.ProductNotFoundException;
import com.backend.ecommerce_api.repository.OrderRepository;
import com.backend.ecommerce_api.repository.ProductRepository;
import com.backend.ecommerce_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String PENDING_STATUS = "PENDING";
    private static final Set<String> ORDER_STATUSES = Set.of("PENDING", "PROCESSING", "SHIPPED", "COMPLETED", "CANCELLED");

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto request) {
        return createOrder(request, null);
    }

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto request, String userEmail) {
        validateRequest(request);

        Product product = productRepository.findByIdForUpdate(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new InsufficientStockException();
        }

        product.setStockQuantity(product.getStockQuantity() - request.getQuantity());
        productRepository.save(product);

        User user = userEmail == null ? null : userRepository.findByEmailIgnoreCase(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        Order order = Order.builder()
                .product(product)
            .user(user)
                .quantity(request.getQuantity())
                .totalAmount(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())))
                .status(PENDING_STATUS)
                .build();

        return OrderResponseDto.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> findMine(String userEmail) {
        return orderRepository.findByUserEmailIgnoreCaseOrderByIdDesc(userEmail).stream()
                .map(OrderResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminOrderResponseDto> findAllForAdmin() {
        return orderRepository.findAll().stream()
                .map(AdminOrderResponseDto::from)
                .toList();
    }

    @Transactional
    public AdminOrderResponseDto updateStatus(Long orderId, String status) {
        String normalizedStatus = status == null ? "" : status.trim().toUpperCase();
        if (!ORDER_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException("Unsupported order status");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setStatus(normalizedStatus);
        return AdminOrderResponseDto.from(orderRepository.save(order));
    }

    private void validateRequest(OrderRequestDto request) {
        if (request == null || request.getProductId() == null || request.getQuantity() == null
                || request.getProductId() <= 0 || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("productId and quantity must be positive values");
        }
    }
}
