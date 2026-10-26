package com.online.bookstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Entity
@Table(name = "BOOKS")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Book Title is Required")
    @Column(name = "bookTitle")
    private String bookTitle;

    @NotBlank(message = "Book Author is Required")
    @Column(name = "bookAuthor")
    private String bookAuthor;

    @NotNull(message = "Book Price is Required")
    @Positive(message = "Book Price should be greater than 0")
    @Column(name = "bookPrice", precision = 12, scale = 2, nullable = false)
    private BigDecimal bookPrice;

    @NotNull(message = "Books Availability is Required")
    @Column(name = "booksAvailable")
    private Integer booksAvailable;

    public Book(){
    }

    public Book(String bookTitle, String bookAuthor, BigDecimal bookPrice, Integer booksAvailable) {
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.bookPrice = bookPrice;
        this.booksAvailable = booksAvailable;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public BigDecimal getBookPrice() {
        return bookPrice;
    }

    public void setBookPrice(BigDecimal bookPrice) {
        this.bookPrice = bookPrice;
    }

    public Integer getBooksAvailable() {
        return booksAvailable;
    }

    public void setBooksAvailable(Integer booksAvailable) {
        this.booksAvailable = booksAvailable;
    }
}
