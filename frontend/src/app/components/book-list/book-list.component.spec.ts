import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { BookListComponent } from './book-list.component';
import { BookService } from '../../services/book.service';
import { Book } from '../../models/book.model';

describe('BookListComponent', () => {
  let component: BookListComponent;
  let fixture: ComponentFixture<BookListComponent>;
  let bookServiceSpy: jasmine.SpyObj<BookService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockBooks: Book[] = [
    {
      id: 1,
      title: 'Clean Code',
      author: 'Robert C. Martin',
      isbn: '9780132350884',
      publicationYear: 2008,
      genre: 'Programming',
    },
    {
      id: 2,
      title: 'The Pragmatic Programmer',
      author: 'David Thomas',
      isbn: '9780135957059',
      publicationYear: 2019,
      genre: 'Programming',
    },
  ];

  beforeEach(async () => {
    bookServiceSpy = jasmine.createSpyObj('BookService', ['getAll', 'delete']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    bookServiceSpy.getAll.and.returnValue(of(mockBooks));

    await TestBed.configureTestingModule({
      imports: [BookListComponent],
      providers: [
        { provide: BookService, useValue: bookServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BookListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load and display books on init', () => {
    expect(bookServiceSpy.getAll).toHaveBeenCalledWith('');
    expect(component.books().length).toBe(2);
  });

  it('should render book titles in the table', () => {
    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(2);
    expect(rows[0].textContent).toContain('Clean Code');
    expect(rows[1].textContent).toContain('The Pragmatic Programmer');
  });

  it('should show error message when service fails', () => {
    bookServiceSpy.getAll.and.returnValue(throwError(() => new Error('Network error')));

    component.loadBooks();
    fixture.detectChanges();

    const alert = fixture.nativeElement.querySelector('.alert--error');
    expect(alert).toBeTruthy();
    expect(alert.textContent).toContain('Could not load books');
  });

  it('should navigate to /books/new when Add Book is clicked', () => {
    const addBtn = fixture.nativeElement.querySelector('.btn--primary');
    addBtn.click();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/books/new']);
  });

  it('should navigate to edit route when Edit is clicked', () => {
    component.editBook(1);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/books', 1, 'edit']);
  });

  it('should call delete and reload books on confirm', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    bookServiceSpy.delete.and.returnValue(of(undefined));

    component.deleteBook(mockBooks[0]);

    expect(bookServiceSpy.delete).toHaveBeenCalledWith(1);
    expect(bookServiceSpy.getAll).toHaveBeenCalledTimes(2); // once on init, once after delete
  });

  it('should NOT call delete when user cancels the confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(false);

    component.deleteBook(mockBooks[0]);

    expect(bookServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should show empty state when no books are returned', () => {
    bookServiceSpy.getAll.and.returnValue(of([]));
    component.loadBooks();
    fixture.detectChanges();

    const emptyState = fixture.nativeElement.querySelector('.empty-state');
    expect(emptyState).toBeTruthy();
  });
});
