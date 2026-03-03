package com.bernabe.bookcollection.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler
 * 
 * Test cases:
 * - BookNotFoundException handling returns 404 with message
 * - DuplicateIsbnException handling returns 409 with message and field
 * - MethodArgumentNotValidException handling returns 400 with validation errors
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleNotFound_ReturnsNotFoundStatusWithMessage() {
        // Arrange
        Long bookId = 123L;
        String expectedMessage = "Book not found with id: " + bookId;
        BookNotFoundException exception = new BookNotFoundException(bookId);

        // Act
        ResponseEntity<Map<String, String>> response = handler.handleNotFound(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedMessage, response.getBody().get("message"));
    }

    @Test
    void testHandleNotFound_WithDifferentMessage_ReturnsCorrectMessage() {
        // Arrange
        Long bookId = 999L;
        String expectedMessage = "Book not found with id: " + bookId;
        BookNotFoundException exception = new BookNotFoundException(bookId);

        // Act
        ResponseEntity<Map<String, String>> response = handler.handleNotFound(exception);

        // Assert
        assertEquals(expectedMessage, response.getBody().get("message"));
    }

    @Test
    void testHandleDuplicateIsbn_ReturnsConflictStatusWithMessageAndField() {
        // Arrange
        String isbn = "9781234567890";
        String expectedMessage = "A book with ISBN " + isbn + " already exists";
        DuplicateIsbnException exception = new DuplicateIsbnException(isbn);

        // Act
        ResponseEntity<Map<String, String>> response = handler.handleDuplicateIsbn(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedMessage, response.getBody().get("message"));
        assertEquals("isbn", response.getBody().get("field"));
        assertEquals(2, response.getBody().size(), "Response body should contain exactly 2 fields");
    }

    @Test
    void testHandleDuplicateIsbn_WithDifferentIsbn_ReturnsCorrectDetails() {
        // Arrange
        String differentIsbn = "978-0-596-52068-7";
        String expectedMessage = "A book with ISBN " + differentIsbn + " already exists";
        DuplicateIsbnException exception = new DuplicateIsbnException(differentIsbn);

        // Act
        ResponseEntity<Map<String, String>> response = handler.handleDuplicateIsbn(exception);

        // Assert
        assertEquals(expectedMessage, response.getBody().get("message"));
        assertEquals("isbn", response.getBody().get("field"));
    }

    @Test
    void testHandleValidation_WithSingleFieldError_ReturnsBadRequestWithDetails() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        // Create a single field error for title field
        FieldError titleError = new FieldError("book", "title", "Title is required");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(titleError));
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleValidation(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        assertEquals("Validation failed", response.getBody().get("message"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals("Title is required", errors.get("title"));
    }

    @Test
    void testHandleValidation_WithMultipleFieldErrors_ReturnsAllErrors() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        // Create multiple field errors
        FieldError titleError = new FieldError("book", "title", "Title is required");
        FieldError authorError = new FieldError("book", "author", "Author is required");
        FieldError isbnError = new FieldError("book", "isbn", "ISBN must be 13 digits");
        
        when(bindingResult.getFieldErrors()).thenReturn(List.of(titleError, authorError, isbnError));
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleValidation(exception);

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        
        assertEquals(3, errors.size());
        assertEquals("Title is required", errors.get("title"));
        assertEquals("Author is required", errors.get("author"));
        assertEquals("ISBN must be 13 digits", errors.get("isbn"));
    }

    @Test
    void testHandleValidation_WithNoErrors_ReturnsEmptyErrorMap() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleValidation(exception);

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    void testHandleValidation_ResponseBodyStructure_ContainsMessageAndErrors() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError error = new FieldError("book", "genre", "Genre is required");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error));
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleValidation(exception);

        // Assert
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("message"));
        assertTrue(body.containsKey("errors"));
        assertEquals(2, body.size(), "Response body should have exactly 2 keys: message and errors");
    }
}
