import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { BookFormComponent } from './book-form.component';
import { BookService } from '../../services/book.service';

describe('BookFormComponent', () => {
  let component: BookFormComponent;
  let fixture: ComponentFixture<BookFormComponent>;
  let bookServiceSpy: jasmine.SpyObj<BookService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const setupComponent = async (paramId?: string) => {
    bookServiceSpy = jasmine.createSpyObj('BookService', ['getById', 'create', 'update']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    if (paramId) {
      bookServiceSpy.getById.and.returnValue(
        of({
          id: Number(paramId),
          title: 'Clean Code',
          author: 'Robert C. Martin',
          isbn: '9780132350884',
          publicationYear: 2008,
          genre: 'Programming',
          description: 'A handbook',
        })
      );
    }

    await TestBed.configureTestingModule({
      imports: [BookFormComponent],
      providers: [
        { provide: BookService, useValue: bookServiceSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => paramId ?? null } } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BookFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  };

  describe('Add mode (no id param)', () => {
    beforeEach(async () => setupComponent());

    it('should create in add mode', () => {
      expect(component).toBeTruthy();
      expect(component.isEditMode).toBeFalse();
    });

    it('should mark form invalid and not submit when required fields are empty', () => {
      component.onSubmit();
      expect(bookServiceSpy.create).not.toHaveBeenCalled();
    });

    it('should call create() and navigate on valid submit', () => {
      component.form.setValue({
        title: 'Test Book',
        author: 'Test Author',
        isbn: '9780132350884',
        publicationYear: 2023,
        genre: 'Fiction',
        description: '',
      });
      bookServiceSpy.create.and.returnValue(of({ id: 3, title: 'Test Book', author: 'Test Author', isbn: '9780132350884', publicationYear: 2023, genre: 'Fiction' }));

      component.onSubmit();

      expect(bookServiceSpy.create).toHaveBeenCalled();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/books']);
    });

    it('should set duplicate ISBN error when 409 is returned', () => {
      component.form.setValue({
        title: 'Test Book',
        author: 'Test Author',
        isbn: '9780132350884',
        publicationYear: 2020,
        genre: 'Fiction',
        description: '',
      });
      bookServiceSpy.create.and.returnValue(
        throwError(() => ({ status: 409, error: { message: 'ISBN already exists' } }))
      );

      component.onSubmit();

      expect(component.f.isbn.errors?.['duplicate']).toBeTrue();
      expect(component.errorMessage).toContain('ISBN');
    });
  });

  describe('Edit mode (with id param)', () => {
    beforeEach(async () => setupComponent('1'));

    it('should be in edit mode and load the book', () => {
      expect(component.isEditMode).toBeTrue();
      expect(bookServiceSpy.getById).toHaveBeenCalledWith(1);
      expect(component.form.value.title).toBe('Clean Code');
    });
  });
});
