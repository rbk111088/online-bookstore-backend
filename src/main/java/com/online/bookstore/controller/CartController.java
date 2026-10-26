package com.online.bookstore.controller;

import com.online.bookstore.dto.AddItemRequest;
import com.online.bookstore.dto.CartItemResponse;
import com.online.bookstore.model.User;
import com.online.bookstore.repository.UserAuthRepository;
import com.online.bookstore.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;
    private final UserAuthRepository userAuthRepository;

    public CartController(CartService cartService, UserAuthRepository userAuthRepository) {
        this.cartService = cartService;
        this.userAuthRepository = userAuthRepository;
    }

    /**
     * Retrieves the cart items for a user.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getCartByUserId(@PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails){
        User currentUser = userAuthRepository.findByUserName(userDetails.getUsername()).orElseThrow();

        if(!currentUser.getId().equals(userId)){
            return ResponseEntity.status(403).body("You can access only your own cart");
        }
        List<CartItemResponse> response = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Adds a book to the user's cart after validating its quantity.
     */
    @PostMapping("/add")
    public ResponseEntity<?> addItem(@Valid @RequestBody AddItemRequest addItemRequest, Authentication authentication){

        User user = userAuthRepository.findByUserName(authentication.getName()).orElseThrow();
        if(!user.getId().equals(addItemRequest.getUserId())){
            return ResponseEntity.status(403).body("You can only modify your own cart");
        }

        cartService.addItem(user, addItemRequest);
        return ResponseEntity.ok("Book Added to Cart Successfully!");
    }
}
