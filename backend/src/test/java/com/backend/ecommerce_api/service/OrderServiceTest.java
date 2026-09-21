package com.backend.ecommerce_api.service;

import com.backend.ecommerce_api.dto.OrderRequestDto;
import com.backend.ecommerce_api.dto.OrderResponseDto;
import com.backend.ecommerce_api.entity.Order;
import com.backend.ecommerce_api.entity.Product;
import com.backend.ecommerce_api.exception.InsufficientStockException;
import com.backend.ecommerce_api.repository.OrderRepository;
import com.backend.ecommerce_api.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderDeductsStockAndCalculatesTotal() {
        Product product = Product.builder()
                .id(1L)
                .name("Keyboard")
                .price(new BigDecimal("25.00"))
                .stockQuantity(10)
                .build();
        Order savedOrder = Order.builder()
                .id(7L)
                .product(product)
                .quantity(2)
                .totalAmount(new BigDecimal("50.00"))
                .status("PENDING")
                .build();

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponseDto response = orderService.createOrder(new OrderRequestDto(1L, 2));

        assertEquals(8, product.getStockQuantity());
        assertEquals(new BigDecimal("50.00"), response.getTotalAmount());
        assertEquals("PENDING", response.getStatus());
        verify(productRepository).save(product);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrderRejectsInsufficientStock() {
        Product product = Product.builder()
                .id(1L)
                .price(new BigDecimal("25.00"))
                .stockQuantity(1)
                .build();
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class,
                () -> orderService.createOrder(new OrderRequestDto(1L, 2)));
    }
}
