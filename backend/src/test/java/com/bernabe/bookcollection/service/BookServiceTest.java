package com.bernabe.bookcollection.service;

import com.bernabe.bookcollection.exception.BookNotFoundException;
import com.bernabe.bookcollection.exception.DuplicateIsbnException;
import com.bernabe.bookcollection.model.Book;
import com.bernabe.bookcollection.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = new Book("Clean Code", "Robert C. Martin", "9780132350884", 2008, "Programming", "A handbook of agile software craftsmanship");
        sampleBook.setId(1L);
    }

    @Test
    void findAll_withNoSearch_returnsAllBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(sampleBook));

        List<Book> result = bookService.findAll(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Clean Code");
        verify(bookRepository, never())
                .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    void findAll_withSearchTerm_delegatesToSearchQuery() {
        when(bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("clean", "clean"))
                .thenReturn(List.of(sampleBook));

        List<Book> result = bookService.findAll("clean");

        assertThat(result).hasSize(1);
        verify(bookRepository, never()).findAll();
    }

    @Test
    void create_withValidBook_savesAndReturnsBook() {
        when(bookRepository.findByIsbn("9780132350884")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        Book result = bookService.create(sampleBook);

        assertThat(result.getTitle()).isEqualTo("Clean Code");
        verify(bookRepository).save(sampleBook);
    }

    @Test
    void create_withDuplicateIsbn_throwsDuplicateIsbnException() {
        when(bookRepository.findByIsbn("9780132350884")).thenReturn(Optional.of(sampleBook));

        Book duplicate = new Book("Another Book", "Jane Doe", "9780132350884", 2020, "Fiction", null);

        assertThatThrownBy(() -> bookService.create(duplicate))
                .isInstanceOf(DuplicateIsbnException.class)
                .hasMessageContaining("9780132350884");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void create_normalizesIsbnByStrippingHyphens() {
        Book bookWithHyphens = new Book("Some Book", "Some Author", "978-0-13-235088-4", 2008, "Tech", null);
        when(bookRepository.findByIsbn("9780132350884")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.create(bookWithHyphens);

        assertThat(result.getIsbn()).isEqualTo("9780132350884");
    }

    @Test
    void findById_withNonExistentId_throwsBookNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findById(99L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_withChangedIsbn_checksForDuplicate() {
        Book updated = new Book("Clean Code", "Robert C. Martin", "9780201633610", 2008, "Programming", "Updated");
        Book existingWithDifferentIsbn = new Book("Another", "Another", "9780201633610", 2000, "Tech", null);
        existingWithDifferentIsbn.setId(2L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookRepository.findByIsbn("9780201633610")).thenReturn(Optional.of(existingWithDifferentIsbn));

        assertThatThrownBy(() -> bookService.update(1L, updated))
                .isInstanceOf(DuplicateIsbnException.class);
    }

    @Test
    void delete_withNonExistentId_throwsBookNotFoundException() {
        when(bookRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.delete(99L))
                .isInstanceOf(BookNotFoundException.class);

        verify(bookRepository, never()).deleteById(any());
    }

    @Test
    void normalizeIsbn_stripsHyphensAndSpaces() {
        assertThat(bookService.normalizeIsbn("978-0-13-235088-4")).isEqualTo("9780132350884");
        assertThat(bookService.normalizeIsbn("978 0 13 235088 4")).isEqualTo("9780132350884");
        assertThat(bookService.normalizeIsbn("9780132350884")).isEqualTo("9780132350884");
    }
}
