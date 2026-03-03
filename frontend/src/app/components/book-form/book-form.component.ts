import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { BookService } from '../../services/book.service';
import { Book } from '../../models/book.model';

@Component({
  selector: 'app-book-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './book-form.component.html',
  styleUrl: './book-form.component.scss',
})
export class BookFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private bookService = inject(BookService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  isEditMode = false;
  bookId?: number;
  submitting = false;
  errorMessage = '';

  // Reactive form - mirrors server-side validation so errors are caught early
  form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(255)]],
    author: ['', [Validators.required, Validators.maxLength(150)]],
    isbn: ['', [Validators.required, Validators.pattern(/^\d{13}$/)]],
    publicationYear: [
      null as number | null,
      [Validators.required, Validators.min(1000), Validators.max(2030)],
    ],
    genre: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', [Validators.maxLength(1000)]],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.bookId = Number(id);
      this.loadBook();
    }
  }

  private loadBook(): void {
    this.bookService.getById(this.bookId!).subscribe({
      next: (book) => this.form.patchValue(book),
      error: () => {
        // Book doesn't exist - bounce back to list
        this.router.navigate(['/books']);
      },
    });
  }

  get f() {
    return this.form.controls;
  }

  isFieldInvalid(field: keyof typeof this.form.controls): boolean {
    const control = this.f[field];
    return control.invalid && (control.dirty || control.touched);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    const bookData = this.form.value as Book;
    const action = this.isEditMode
      ? this.bookService.update(this.bookId!, bookData)
      : this.bookService.create(bookData);

    action.subscribe({
      next: () => this.router.navigate(['/books']),
      error: (err) => {
        this.submitting = false;
        // 409 Conflict means duplicate ISBN
        if (err.status === 409) {
          this.f.isbn.setErrors({ duplicate: true });
          this.errorMessage = err.error?.message ?? 'This ISBN is already registered.';
        } else {
          this.errorMessage = 'Something went wrong. Please try again.';
        }
      },
    });
  }

  cancel(): void {
    this.router.navigate(['/books']);
  }
}
