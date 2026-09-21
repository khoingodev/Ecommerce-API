package com.backend.ecommerce_api.repository;

import com.backend.ecommerce_api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
