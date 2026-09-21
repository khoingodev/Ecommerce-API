package com.backend.ecommerce_api.controller;

import com.backend.ecommerce_api.dto.OrderRequestDto;
import com.backend.ecommerce_api.dto.OrderResponseDto;
import com.backend.ecommerce_api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/mine")
    public ResponseEntity<java.util.List<OrderResponseDto>> findMine(
            org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(orderService.findMine(authentication.getName()));
    }

    @PostMapping
        public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderRequestDto request,
            org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request, authentication.getName()));
    }
}
