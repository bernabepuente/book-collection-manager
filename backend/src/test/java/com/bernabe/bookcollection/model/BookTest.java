package com.bernabe.bookcollection.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testBookCreation_WithValidData_ShouldSucceed() {
        // Arrange & Act
        Book book = new Book("Clean Code", "Robert Martin", "9780132350884", 2008, "Programming", "A guide to writing clean code");

        // Assert
        assertNotNull(book);
        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert Martin", book.getAuthor());
        assertEquals("9780132350884", book.getIsbn());
        assertEquals(2008, book.getPublicationYear());
        assertEquals("Programming", book.getGenre());
        assertEquals("A guide to writing clean code", book.getDescription());
        assertNull(book.getId()); // ID should be null before persistence
    }

    @Test
    void testBookCreation_WithDefaultConstructor_ShouldSucceed() {
        // Arrange & Act
        Book book = new Book();

        // Assert
        assertNotNull(book);
        assertNull(book.getId());
        assertNull(book.getTitle());
        assertNull(book.getAuthor());
        assertNull(book.getIsbn());
        assertNull(book.getPublicationYear());
        assertNull(book.getGenre());
        assertNull(book.getDescription());
    }

    @Test
    void testBookCreation_WithSetters_ShouldSucceed() {
        // Arrange
        Book book = new Book();

        // Act
        book.setId(1L);
        book.setTitle("Test Title");
        book.setAuthor("Test Author");
        book.setIsbn("9781234567890");
        book.setPublicationYear(2020);
        book.setGenre("Fiction");
        book.setDescription("Test description");

        // Assert
        assertEquals(1L, book.getId());
        assertEquals("Test Title", book.getTitle());
        assertEquals("Test Author", book.getAuthor());
        assertEquals("9781234567890", book.getIsbn());
        assertEquals(2020, book.getPublicationYear());
        assertEquals("Fiction", book.getGenre());
        assertEquals("Test description", book.getDescription());
    }

    @Test
    void testValidation_WithBlankTitle_ShouldFail() {
        // Arrange
        Book book = new Book("", "Author", "9781234567890", 2020, "Genre", "Description");

        // Act
        Set<ConstraintViolation<Book>> violations = validator.validate(book);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("title") 
                        && v.getMessage().equals("Title is required")));
    }

    @Test
    void testValidation_WithTitleExceedingMaxLength_ShouldFail() {
        // Arrange
        String longTitle = "a".repeat(256);
        Book book = new Book(longTitle, "Author", "9781234567890", 2020, "Genre", "Description");

        // Act
        Set<ConstraintViolation<Book>> violations = validator.validate(book);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("title") 
                        && v.getMessage().equals("Title must be under 255 characters")));
    }

    @Test
    void testValidation_WithBlankAuthor_ShouldFail() {
        // Arrange
        Book book = new Book("Title", "   ", "9781234567890", 2020, "Genre", "Description");

        // Act
        Set<ConstraintViolation<Book>> violations = validator.validate(book);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("author") 
                        && v.getMessage().equals("Author is required")));
    }

    @Test
    void testValidation_WithAuthorExceedingMaxLength_ShouldFail() {
        // Arrange
        String longAuthor = "a".repeat(151);
        Book book = new Book("Title", longAuthor, "9781234567890", 2020, "Genre", "Description");

        // Act
        Set<ConstraintViolation<Book>> violations = validator.validate(book);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("author") 
                        && v.getMessage().equals("Author name must be under 150 characters")));
    }

    @Test
    void testValidation_WithBlankIsbn_ShouldFail() {
        // Arrange
        Book book = new Book("Title", "Author", "", 2020, "Genre", "Description");

        // Act
        Set<ConstraintViolation<Book>> violations = validator.validate(book);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("isbn")));
    }

    @Test
    void testValidation_WithInvalidIsbnPattern_ShouldFail() {
        // Arrange
        Book book = new Book("Title", "Author", "12345", 2020, "Genre", "Description");

        // Act
        Set<ConstraintViolation<Book>> violations = validator.validate(book);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("isbn") 
                        && v.getMessage().contains("ISBN must be 13 digits")));
    }

    @Test
    void testValidation_WithIsbnContainingNonDigits_ShouldFail() {
        // Arrange
        Book book = new Book("Title", "Author", "978-123456789", 2020, "Genre", "Description");

        // Act
        Set<ConstraintViolation<Book>> violations = validator.validate(book);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("isbn")));
    }

    @Test
    void testValidation_WithNullPublicationYear_ShouldFail() {
        // Arrange
        Book book = new Book("Title", "Author", "9781234567890", null, "Genre", "Description");

        // Act
        Set<ConstraintViolation<Book>> violations = validator.validate(book);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("publicationYear") 
                        && v.getMessage().equals("Publication year is required")));
    }

    @Test
    void testValidation_WithPublicationYearBelowMinimum_ShouldFail() {
        // Arrange
        Book book = new Book("Title", "Author", "9781234567890", 999, "Genre", "Description");

        // Act
        Set<ConstraintViolation<Book>> violations = validator.validate(book);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("publicationYear") 
                        && v.getMessage().equals("Publication year must be at least 1000")));
    }

    @Test
    void testValidation_WithPublicationYearAboveMaximum_ShouldFail() {
        // Arrange
        Book book = new Book("Title", "Author", "9781234567890", 2031, "Genre", "Description");

        // Act
        Set<ConstraintViolation<Book>> violations = validator.validate(book);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("publicationYear") 
                        && v.getMessage().equals("Publication year seems too far in the future")));
    }

    @Test
    void testValidation_WithPublicationYearAtBoundaries_ShouldSucceed() {
        // Test minimum boundary
        Book book1 = new Book("Title", "Author", "9781234567890", 1000, "Genre", "Description");
        Set<ConstraintViolation<Book>> violations1 = validator.validate(book1);
        assertTrue(violations1.isEmpty());

        // Test maximum boundary
        Book book2 = new Book("Title", "Author", "9781234567891", 2030, "Genre", "Description");
        Set<ConstraintViolation<Book>> violations2 = validator.validate(book2);
        assertTrue(violations2.isEmpty());
    }

    @Test
    void testValidation_WithBlankGenre_ShouldFail() {
        // Arrange
        Book book = new Book("Title", "Author", "9781234567890", 2020, "", "Description");

        // Act
        Set<ConstraintViolation<Book>> violations = validator.validate(book);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("genre") 
                        && v.getMessage().equals("Genre is required")));
    }

    @Test
    void testValidation_WithGenreExceedingMaxLength_ShouldFail() {
        // Arrange
        String longGenre = "a".repeat(101);
        Book book = new Book("Title", "Author", "9781234567890", 2020, longGenre, "Description");

        // Act
        Set<ConstraintViolation<Book>> violations = validator.validate(book);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("genre")));
    }

    @Test
    void testValidation_WithDescriptionExceedingMaxLength_ShouldFail() {
        // Arrange
        String longDescription = "a".repeat(1001);
        Book book = new Book("Title", "Author", "9781234567890", 2020, "Genre", longDescription);

        // Act
        Set<ConstraintViolation<Book>> violations = validator.validate(book);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("description") 
                        && v.getMessage().equals("Description must be under 1000 characters")));
    }

    @Test
    void testValidation_WithNullDescription_ShouldSucceed() {
        // Arrange
        Book book = new Book("Title", "Author", "9781234567890", 2020, "Genre", null);

        // Act
        Set<ConstraintViolation<Book>> violations = validator.validate(book);

        // Assert
        assertTrue(violations.isEmpty(), "Description is optional, should be valid with null");
    }

    @Test
    void testValidation_WithValidBook_ShouldSucceed() {
        // Arrange
        Book book = new Book("The Pragmatic Programmer", "Andrew Hunt", "9780135957059", 2019, "Technology", "Your journey to mastery");

        // Act
        Set<ConstraintViolation<Book>> violations = validator.validate(book);

        // Assert
        assertTrue(violations.isEmpty(), "Valid book should have no validation errors");
    }
}
