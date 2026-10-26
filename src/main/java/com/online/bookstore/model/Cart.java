package com.online.bookstore.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CART",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_book_cart_user",
            columnNames = {"userId", "bookId"})
        })
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "bookId", nullable = false)
    private Book book;
    private int bookQuantity;

    public Cart(){}

    public Cart(User user, Book book, int bookQuantity) {
        this.user = user;
        this.book = book;
        this.bookQuantity = bookQuantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public int getBookQuantity() {
        return bookQuantity;
    }

    public void setBookQuantity(int bookQuantity) {
        this.bookQuantity = bookQuantity;
    }
}
