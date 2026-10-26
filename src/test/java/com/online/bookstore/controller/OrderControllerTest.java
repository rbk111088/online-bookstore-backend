package com.online.bookstore.controller;

import com.online.bookstore.model.Book;
import com.online.bookstore.model.Order;
import com.online.bookstore.model.User;
import com.online.bookstore.repository.UserAuthRepository;
import com.online.bookstore.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {
    private OrderService orderService;
    private UserAuthRepository userRepository;
    private OrderController controller;
    private Authentication authentication;
    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        userRepository = mock(UserAuthRepository.class);

        controller = new OrderController(orderService, userRepository);
        authentication = mock(Authentication.class);

        user = new User("reader", "encoded");
        user.setId(1L);

        when(authentication.getName()).thenReturn("reader");
        when(userRepository.findByUserName("reader"))
                .thenReturn(Optional.of(user));
    }

    @Test
    void rejectsOrderForDifferentUser() {
        ResponseEntity<?> response =
                controller.orderItem(9L, authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(orderService, never()).placeOrder(anyLong());
    }

    @Test
    void rejectsEmptyCart() {
        when(orderService.placeOrder(1L))
                .thenThrow(new IllegalArgumentException(
                        "Shopping cart is empty for the user ID: 1"));

        ResponseEntity<?> response =
                controller.orderItem(1L, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "Shopping cart is empty for the user ID: 1",
                response.getBody());

        verify(orderService).placeOrder(1L);
    }

    @Test
    void rejectsOrderWhenStockIsInsufficient() {
        when(orderService.placeOrder(1L))
                .thenThrow(new IllegalArgumentException(
                        "Insufficient Book Quantity : Java"));

        ResponseEntity<?> response =
                controller.orderItem(1L, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "Insufficient Book Quantity : Java",
                response.getBody());
    }

    @Test
    void placesOrderSuccessfully() {
        Order expectedOrder = new Order();
        expectedOrder.setUserId(1L);
        expectedOrder.setTotalPrice(new BigDecimal("200.00"));
        expectedOrder.setOrderStatus("CONFIRMED");

        when(orderService.placeOrder(1L)).thenReturn(expectedOrder);

        ResponseEntity<?> response =
                controller.orderItem(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Order savedOrder = (Order) response.getBody();
        assertNotNull(savedOrder);
        assertEquals("CONFIRMED", savedOrder.getOrderStatus());
        assertEquals(
                0,
                savedOrder.getTotalPrice()
                        .compareTo(new BigDecimal("200.00")));

        verify(orderService).placeOrder(1L);
    }

    @Test
    void returnsOrdersForCurrentUser() {
        List<Order> expectedOrders = List.of(new Order());

        when(orderService.getOrdersByUserId(1L))
                .thenReturn(expectedOrders);

        ResponseEntity<?> response =
                controller.getOrdersByUserId(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedOrders, response.getBody());

        verify(orderService).getOrdersByUserId(1L);
    }

    @Test
    void rejectsOrderHistoryForDifferentUser() {
        ResponseEntity<?> response =
                controller.getOrdersByUserId(9L, authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(orderService, never()).getOrdersByUserId(anyLong());
    }
}
