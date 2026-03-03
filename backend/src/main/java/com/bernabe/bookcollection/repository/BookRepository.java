package com.bernabe.bookcollection.repository;

import com.bernabe.bookcollection.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Used for search - case-insensitive partial match on title OR author
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);

    Optional<Book> findByIsbn(String isbn);
}
