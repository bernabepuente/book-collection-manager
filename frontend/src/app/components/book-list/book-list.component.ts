import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { BookService } from '../../services/book.service';
import { Book } from '../../models/book.model';

@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './book-list.component.html',
  styleUrl: './book-list.component.scss',
})
export class BookListComponent implements OnInit {
  private bookService = inject(BookService);
  private router = inject(Router);

  books = signal<Book[]>([]);
  loading = signal(false);
  errorMessage = signal('');
  searchTerm = '';

  ngOnInit(): void {
    this.loadBooks();
  }

  loadBooks(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.bookService.getAll(this.searchTerm).subscribe({
      next: (books) => {
        this.books.set(books);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not load books. Is the backend running?');
        this.loading.set(false);
      },
    });
  }

  onSearch(): void {
    this.loadBooks();
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.loadBooks();
  }

  addBook(): void {
    this.router.navigate(['/books/new']);
  }

  editBook(id: number): void {
    this.router.navigate(['/books', id, 'edit']);
  }

  deleteBook(book: Book): void {
    if (!confirm(`Delete "${book.title}"? This cannot be undone.`)) return;

    this.bookService.delete(book.id!).subscribe({
      next: () => this.loadBooks(),
      error: () => this.errorMessage.set('Failed to delete book. Please try again.'),
    });
  }
}
