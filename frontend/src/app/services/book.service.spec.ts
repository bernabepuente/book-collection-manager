import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { BookService } from './book.service';
import { Book } from '../models/book.model';
import { environment } from '../../environments/environment';

describe('BookService', () => {
  let service: BookService;
  let httpMock: HttpTestingController;

  const API_URL = `${environment.apiUrl}/api/books`;

  const mockBook: Book = {
    id: 1,
    title: 'Clean Code',
    author: 'Robert C. Martin',
    isbn: '9780132350884',
    publicationYear: 2008,
    genre: 'Programming',
    description: 'A handbook of agile software craftsmanship',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BookService],
    });

    service = TestBed.inject(BookService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAll() should call GET /api/books with no params when no search term', () => {
    service.getAll().subscribe((books) => {
      expect(books.length).toBe(1);
      expect(books[0].title).toBe('Clean Code');
    });

    const req = httpMock.expectOne(`${API_URL}`);
    expect(req.request.method).toBe('GET');
    req.flush([mockBook]);
  });

  it('getAll() with search term should include search query param', () => {
    service.getAll('clean').subscribe();

    const req = httpMock.expectOne((r) => r.url === API_URL && r.params.get('search') === 'clean');
    expect(req.request.method).toBe('GET');
    req.flush([mockBook]);
  });

  it('getAll() should not add search param for whitespace-only input', () => {
    service.getAll('   ').subscribe();

    const req = httpMock.expectOne(API_URL);
    expect(req.request.params.has('search')).toBeFalse();
    req.flush([]);
  });

  it('create() should POST to /api/books and return created book', () => {
    const newBook: Book = { ...mockBook, id: undefined };
    service.create(newBook).subscribe((book) => {
      expect(book.id).toBe(1);
    });

    const req = httpMock.expectOne(API_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newBook);
    req.flush(mockBook);
  });

  it('update() should PUT to /api/books/:id', () => {
    service.update(1, mockBook).subscribe();

    const req = httpMock.expectOne(`${API_URL}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockBook);
  });

  it('delete() should call DELETE /api/books/:id', () => {
    service.delete(1).subscribe();

    const req = httpMock.expectOne(`${API_URL}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
