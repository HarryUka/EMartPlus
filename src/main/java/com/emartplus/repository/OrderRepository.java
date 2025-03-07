package com.emartplus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emartplus.entity.Order;
import com.emartplus.entity.User;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
} 