package com.online.bookstore.controller;

import com.online.bookstore.model.*;
import com.online.bookstore.repository.UserAuthRepository;
import com.online.bookstore.service.OrderService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserAuthRepository userAuthRepository;

    public OrderController(OrderService orderService, UserAuthRepository userAuthRepository) {
        this.orderService = orderService;
        this.userAuthRepository = userAuthRepository;
    }

    /**
     * Places an order using the items in the user's cart.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Long userId, Authentication authentication){
        User currentUser = userAuthRepository.findByUserName(authentication.getName()).orElseThrow();

        if(!currentUser.getId().equals(userId)){
            return ResponseEntity.status(403).body("You can only access your own orders");
        }

        List<Order> orderList = orderService.getOrdersByUserId(userId);

        return ResponseEntity.ok(orderList);
    }

    /**
     * Retrieves the orders placed by a user.
     */
    @PostMapping("/orderItem/{userId}")
    @Transactional
    public ResponseEntity<?> orderItem(@PathVariable Long userId,  Authentication authentication){

        User currentUser = userAuthRepository.findByUserName(authentication.getName()).orElseThrow();

        if(!currentUser.getId().equals(userId)){
            return ResponseEntity.status(403).body("You can only place orders for your own account");
        }

        try{
            Order saveOrder= orderService.placeOrder(userId);
            return ResponseEntity.ok(saveOrder);
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
