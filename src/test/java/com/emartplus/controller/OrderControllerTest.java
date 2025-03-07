package com.emartplus.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.emartplus.dto.OrderRequest;
import com.emartplus.entity.Order;
import com.emartplus.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @WithMockUser
    void shouldCreateOrder() throws Exception {
        // Arrange
        OrderRequest orderRequest = new OrderRequest();
        Order order = new Order();
        order.setId(1L);
        order.setTotalAmount(new BigDecimal("20.00"));

        when(orderService.createOrder(any(), anyString())).thenReturn(order);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.totalAmount").value("20.00"));
    }

    @Test
    @WithMockUser
    void shouldGetUserOrders() throws Exception {
        // Arrange
        Order order = new Order();
        order.setId(1L);
        when(orderService.getUserOrders(anyString())).thenReturn(Arrays.asList(order));

        // Act & Assert
        mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser
    void shouldCancelOrder() throws Exception {
        // Arrange
        Order order = new Order();
        order.setId(1L);
        when(orderService.cancelOrder(eq(1L), anyString())).thenReturn(order);

        // Act & Assert
        mockMvc.perform(post("/api/orders/1/cancel"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }
} 