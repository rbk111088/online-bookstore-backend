package com.online.bookstore.service;

import com.online.bookstore.dto.AddItemRequest;
import com.online.bookstore.dto.CartItemResponse;
import com.online.bookstore.model.Book;
import com.online.bookstore.model.Cart;
import com.online.bookstore.model.User;
import com.online.bookstore.repository.BookRepository;
import com.online.bookstore.repository.CartRepository;
import com.online.bookstore.repository.UserAuthRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {
    private final BookRepository bookRepository;
    private final CartRepository cartRepository;
    private final UserAuthRepository userAuthRepository;

    public CartService(BookRepository bookRepository, CartRepository cartRepository, UserAuthRepository userAuthRepository) {
        this.bookRepository = bookRepository;
        this.cartRepository = cartRepository;
        this.userAuthRepository = userAuthRepository;
    }

    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartByUserId(Long userId) {
        User user = userAuthRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return cartRepository.findByUserId(userId).stream()
                .map(cart -> new CartItemResponse(
                        cart.getId(),
                        user.getId(),
                        cart.getBook().getId(),
                        cart.getBook().getBookTitle(),
                        cart.getBook().getBookPrice(),
                        cart.getBookQuantity()))
                .toList();
    }

    @Transactional
    public void addItem(User user, AddItemRequest request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        Cart cart = cartRepository
                .findByUserIdAndBookId(user.getId(), book.getId())
                .orElse(null);

        int existingQuantity = cart == null ? 0 : cart.getBookQuantity();
        int requestedQuantity;

        try {
            requestedQuantity = Math.addExact(
                    existingQuantity, request.getBookQuantity());
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Requested quantity is too large");
        }

        if (request.getBookQuantity() < 1
                || book.getBooksAvailable() == null
                || book.getBooksAvailable() < requestedQuantity) {
            throw new IllegalArgumentException("Insufficient book quantity");
        }

        if (cart == null) {
            cart = new Cart(user, book, request.getBookQuantity());
        } else {
            cart.setBookQuantity(requestedQuantity);
        }

        cartRepository.save(cart);
    }
}
