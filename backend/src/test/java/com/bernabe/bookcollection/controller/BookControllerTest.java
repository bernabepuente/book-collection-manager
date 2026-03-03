package com.bernabe.bookcollection.controller;

import com.bernabe.bookcollection.exception.DuplicateIsbnException;
import com.bernabe.bookcollection.exception.GlobalExceptionHandler;
import com.bernabe.bookcollection.model.Book;
import com.bernabe.bookcollection.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import(GlobalExceptionHandler.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Test
    void getAll_returnsListOfBooks() throws Exception {
        Book book = new Book("Clean Code", "Robert C. Martin", "9780132350884", 2008, "Programming", null);
        book.setId(1L);
        when(bookService.findAll(null)).thenReturn(List.of(book));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[0].isbn").value("9780132350884"));
    }

    @Test
    void create_withInvalidBody_returns400WithFieldErrors() throws Exception {
        // Missing required fields
        String payload = "{\"title\": \"\", \"author\": \"\"}";

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void create_withDuplicateIsbn_returns409() throws Exception {
        Book book = new Book("Some Book", "Some Author", "9780132350884", 2020, "Fiction", null);
        when(bookService.create(any(Book.class))).thenThrow(new DuplicateIsbnException("9780132350884"));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A book with ISBN 9780132350884 already exists"));
    }
}
