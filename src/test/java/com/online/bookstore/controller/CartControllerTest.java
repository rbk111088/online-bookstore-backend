package com.online.bookstore.controller;

import com.online.bookstore.dto.AddItemRequest;
import com.online.bookstore.model.User;
import com.online.bookstore.repository.UserAuthRepository;
import com.online.bookstore.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CartControllerTest {

    private UserAuthRepository userAuthRepository;
    private CartController cartController;
    private Authentication authentication;
    private CartService cartService;
    private User user;

    @BeforeEach
    void setUp() {
        cartService = mock(CartService.class);
        userAuthRepository = mock(UserAuthRepository.class);
        authentication = mock(Authentication.class);

        cartController = new CartController(cartService, userAuthRepository);

        user = new User("reader1", "encoded-password");
        user.setId(1L);

        when(authentication.getName()).thenReturn("reader1");
        when(userAuthRepository.findByUserName("reader1"))
                .thenReturn(Optional.of(user));
    }

    @Test
    void addItemDelegatesToServiceWhenUserIsAuthorized() {
        AddItemRequest request = request(1L, 2L, 2);

        ResponseEntity<?> response =
                cartController.addItem(request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cartService).addItem(user, request);
    }

    @Test
    void addItemRejectsRequestWhenUserIsUnauthorized() {
        AddItemRequest request = request(99L, 2L, 1);

        ResponseEntity<?> response =
                cartController.addItem(request, authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(cartService, never())
                .addItem(any(User.class), any(AddItemRequest.class));
    }

    private AddItemRequest request(Long userId, Long bookId, int quantity) {
        AddItemRequest request = new AddItemRequest();
        request.setUserId(userId);
        request.setBookId(bookId);
        request.setBookQuantity(quantity);
        return request;
    }
}