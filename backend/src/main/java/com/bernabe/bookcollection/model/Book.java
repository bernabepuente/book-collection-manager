package com.bernabe.bookcollection.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be under 255 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 150, message = "Author name must be under 150 characters")
    @Column(nullable = false)
    private String author;

    // Stored normalized (digits only, no hyphens). Normalization happens in BookService.
    // We accept ISBN-13 (13 digits) only. ISBN-10 is legacy and out of scope for this app.
    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^\\d{13}$", message = "ISBN must be 13 digits (hyphens are stripped automatically)")
    @Column(nullable = false, unique = true)
    private String isbn;

    @NotNull(message = "Publication year is required")
    @Min(value = 1000, message = "Publication year must be at least 1000")
    @Max(value = 2030, message = "Publication year seems too far in the future")
    @Column(nullable = false)
    private Integer publicationYear;

    @NotBlank(message = "Genre is required")
    @Size(max = 100)
    @Column(nullable = false)
    private String genre;

    // Optional but encouraged - reviewers like context
    @Size(max = 1000, message = "Description must be under 1000 characters")
    @Column(length = 1000)
    private String description;

    // -- Constructors --

    public Book() {}

    public Book(String title, String author, String isbn, Integer publicationYear, String genre, String description) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.genre = genre;
        this.description = description;
    }

    // -- Getters & Setters --

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }

    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }

    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Integer getPublicationYear() { return publicationYear; }

    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public String getGenre() { return genre; }

    public void setGenre(String genre) { this.genre = genre; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}
