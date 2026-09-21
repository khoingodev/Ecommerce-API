package com.backend.ecommerce_api.controller;

import com.backend.ecommerce_api.dto.AdminOrderResponseDto;
import com.backend.ecommerce_api.dto.OrderStatusRequestDto;
import com.backend.ecommerce_api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<AdminOrderResponseDto>> findAll() {
        return ResponseEntity.ok(orderService.findAllForAdmin());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminOrderResponseDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusRequestDto request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.getStatus()));
    }
}
