package com.online.bookstore.service;

import com.online.bookstore.model.*;
import com.online.bookstore.repository.BookRepository;
import com.online.bookstore.repository.CartRepository;
import com.online.bookstore.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderService {

    private final BookRepository bookRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    public OrderService(BookRepository bookRepository, CartRepository cartRepository, OrderRepository orderRepository) {
        this.bookRepository = bookRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(@PathVariable Long userId){
        return orderRepository.findByUserId(userId);
    }

    @Transactional
    public Order placeOrder(Long userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException(
                    "Shopping cart is empty for the user ID: " + userId);
        }

        // Aggregate quantities by book ID to avoid incorrect stock deductions.
        Map<Long, Integer> requestedQuantities = new TreeMap<>();

        for (Cart cartItem : cartItems) {
            int quantity = cartItem.getBookQuantity();

            if (quantity < 1) {
                throw new IllegalArgumentException("Invalid book quantity");
            }

            try{
                requestedQuantities.merge(
                        cartItem.getBook().getId(), quantity, Math::addExact);
            } catch (ArithmeticException e){
                throw new IllegalArgumentException("Requested Book Quantity is large");
            }
        }

        // Lock books in ascending ID order to reduce deadlock risk.
        Map<Long, Book> lockedBooks = new LinkedHashMap<>();

        for (Map.Entry<Long, Integer> entry : requestedQuantities.entrySet()) {
            Long bookId = entry.getKey();

            Book book = bookRepository.findByIdForUpdate(bookId)
                    .orElseThrow(() -> new IllegalStateException(
                            "No longer book exists: " + bookId));

            if (book.getBooksAvailable() == null
                    || book.getBooksAvailable() < entry.getValue()) {
                throw new IllegalArgumentException(
                        "Insufficient Book Quantity : " + book.getBookTitle());
            }

            lockedBooks.put(bookId, book);
        }

        List<OrderItems> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : requestedQuantities.entrySet()) {
            Book book = lockedBooks.get(entry.getKey());
            int quantity = entry.getValue();

            BigDecimal unitPrice = book.getBookPrice();
            totalPrice = totalPrice.add(
                    unitPrice.multiply(BigDecimal.valueOf(quantity)));

            orderItems.add(new OrderItems(
                    book.getId(),
                    book.getBookTitle(),
                    unitPrice,
                    quantity));

            book.setBooksAvailable(book.getBooksAvailable() - quantity);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setTotalPrice(totalPrice);
        order.setOrderStatus("CONFIRMED");
        order.setOrderItem(orderItems);

        Order savedOrder = orderRepository.save(order);
        cartRepository.deleteAll(cartItems);

        return savedOrder;
    }
}
