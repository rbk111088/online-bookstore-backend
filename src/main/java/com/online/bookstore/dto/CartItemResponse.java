package com.online.bookstore.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long cartId,
        Long userId,
        Long bookId,
        String bookTitle,
        BigDecimal bookPrice,
        Integer bookQuantity) {
}
