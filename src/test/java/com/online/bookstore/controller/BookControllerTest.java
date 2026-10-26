package com.online.bookstore.controller;

import com.online.bookstore.model.Book;
import com.online.bookstore.repository.BookRepository;
import com.online.bookstore.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookControllerTest {
    @Mock
    private BookService bookService;
    private BookController controller;

    @BeforeEach void setUp() {
        bookService = mock(BookService.class);
        controller = new BookController(bookService);
    }

    @Test void returnsAvailableBooks() {
        List<Book> books = List.of(new Book("Java", "Author", new BigDecimal(100.00), 5));
        when(bookService.getAllBooks()).thenReturn(books);
        ResponseEntity<List<Book>> response = controller.getAllBooks();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(bookService).getAllBooks();
    }

    @Test void returnsEmptyListWhenNoBooksExist() {
        when(bookService.getAllBooks()).thenReturn(List.of());
        ResponseEntity<List<Book>> response = controller.getAllBooks();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }
}
