package com.backend.ecommerce_api.repository;

import com.backend.ecommerce_api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByUserEmailIgnoreCaseOrderByIdDesc(String email);
}
