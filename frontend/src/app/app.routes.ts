import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'books', pathMatch: 'full' },
  {
    path: 'books',
    loadComponent: () =>
      import('./components/book-list/book-list.component').then((m) => m.BookListComponent),
  },
  {
    path: 'books/new',
    loadComponent: () =>
      import('./components/book-form/book-form.component').then((m) => m.BookFormComponent),
  },
  {
    path: 'books/:id/edit',
    loadComponent: () =>
      import('./components/book-form/book-form.component').then((m) => m.BookFormComponent),
  },
  { path: '**', redirectTo: 'books' },
];
