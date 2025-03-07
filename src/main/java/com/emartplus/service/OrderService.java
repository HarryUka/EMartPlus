package com.emartplus.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emartplus.dto.OrderItemRequest;
import com.emartplus.dto.OrderRequest;
import com.emartplus.entity.Order;
import com.emartplus.entity.OrderItem;
import com.emartplus.entity.OrderStatus;
import com.emartplus.entity.Product;
import com.emartplus.entity.User;
import com.emartplus.exception.ApiException;
import com.emartplus.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final UserService userService;

    @Transactional
    public Order createOrder(OrderRequest orderRequest, String email) {
        User user = userService.getUserByEmail(email);
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            Product product = productService.getProduct(itemRequest.getProductId());
            
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new ApiException("Insufficient stock for product: " + product.getName(), 
                    HttpStatus.BAD_REQUEST);
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice());
            
            order.getItems().add(orderItem);
            totalAmount = totalAmount.add(product.getPrice()
                .multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            // Update stock
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
        }

        order.setTotalAmount(totalAmount);
        return orderRepository.save(order);
    }

    public List<Order> getUserOrders(String email) {
        User user = userService.getUserByEmail(email);
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    public Order getOrder(Long id, String email) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));

        if (!order.getUser().getEmail().equals(email)) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        return order;
    }

    @Transactional
    public Order cancelOrder(Long id, String email) {
        Order order = getOrder(id, email);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ApiException("Order cannot be cancelled", HttpStatus.BAD_REQUEST);
        }

        order.setStatus(OrderStatus.CANCELLED);

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }

        return orderRepository.save(order);
    }
} 