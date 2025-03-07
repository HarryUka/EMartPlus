package com.emartplus.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.emartplus.dto.OrderItemRequest;
import com.emartplus.dto.OrderRequest;
import com.emartplus.entity.Order;
import com.emartplus.entity.OrderItem;
import com.emartplus.entity.OrderStatus;
import com.emartplus.entity.Product;
import com.emartplus.entity.User;
import com.emartplus.exception.ApiException;
import com.emartplus.repository.OrderRepository;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private ProductService productService;

    @MockBean
    private UserService userService;

    @Test
    void shouldCreateOrder() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("10.00"));
        product.setStockQuantity(10);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(Arrays.asList(itemRequest));

        when(userService.getUserByEmail("test@example.com")).thenReturn(user);
        when(productService.getProduct(1L)).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Order order = orderService.createOrder(orderRequest, "test@example.com");

        // Assert
        assertNotNull(order);
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(new BigDecimal("20.00"), order.getTotalAmount());
        assertEquals(1, order.getItems().size());
        assertEquals(8, product.getStockQuantity());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientStock() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setStockQuantity(1);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(Arrays.asList(itemRequest));

        when(userService.getUserByEmail(anyString())).thenReturn(new User());
        when(productService.getProduct(1L)).thenReturn(product);

        // Act & Assert
        assertThrows(ApiException.class, 
            () -> orderService.createOrder(orderRequest, "test@example.com"));
    }

    @Test
    void shouldCancelOrder() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");

        Product product = new Product();
        product.setStockQuantity(8);

        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.getItems().add(createOrderItem(product, 2));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Order cancelledOrder = orderService.cancelOrder(1L, "test@example.com");

        // Assert
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
        assertEquals(10, product.getStockQuantity());
    }

    private OrderItem createOrderItem(Product product, int quantity) {
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }
} 