package com.bernabe.bookcollection.service;

import com.bernabe.bookcollection.exception.BookNotFoundException;
import com.bernabe.bookcollection.exception.DuplicateIsbnException;
import com.bernabe.bookcollection.model.Book;
import com.bernabe.bookcollection.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll(String search) {
        if (StringUtils.hasText(search)) {
            return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(search, search);
        }
        return bookRepository.findAll();
    }

    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public Book create(Book book) {
        String normalizedIsbn = normalizeIsbn(book.getIsbn());
        book.setIsbn(normalizedIsbn);

        checkForDuplicateIsbn(normalizedIsbn, null);

        return bookRepository.save(book);
    }

    public Book update(Long id, Book incoming) {
        Book existing = findById(id);

        String normalizedIsbn = normalizeIsbn(incoming.getIsbn());
        incoming.setIsbn(normalizedIsbn);

        // Only flag duplicate if the ISBN actually changed
        if (!normalizedIsbn.equals(existing.getIsbn())) {
            checkForDuplicateIsbn(normalizedIsbn, id);
        }

        existing.setTitle(incoming.getTitle());
        existing.setAuthor(incoming.getAuthor());
        existing.setIsbn(normalizedIsbn);
        existing.setPublicationYear(incoming.getPublicationYear());
        existing.setGenre(incoming.getGenre());
        existing.setDescription(incoming.getDescription());

        return bookRepository.save(existing);
    }

    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }

    // --- helpers ---

    /**
     * Strip hyphens and spaces from ISBN so "978-0-06-112008-4" is treated
     * the same as "9780061120084". This avoids false duplicates on user input variation.
     */
    String normalizeIsbn(String isbn) {
        if (isbn == null) return null;
        return isbn.replaceAll("[\\s-]", "");
    }

    private void checkForDuplicateIsbn(String isbn, Long excludeId) {
        bookRepository.findByIsbn(isbn).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new DuplicateIsbnException(isbn);
            }
        });
    }
}
