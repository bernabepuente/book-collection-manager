import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <header class="app-header">
      <div class="app-header__inner">
        <a routerLink="/books" class="app-header__logo">
          📚 Book Collection
        </a>
      </div>
    </header>

    <main class="app-main">
      <router-outlet />
    </main>
  `,
  styles: [`
    .app-header {
      background: #fff;
      border-bottom: 1px solid #e5e7eb;
      position: sticky;
      top: 0;
      z-index: 10;
    }

    .app-header__inner {
      max-width: 1100px;
      margin: 0 auto;
      padding: 0 24px;
      height: 56px;
      display: flex;
      align-items: center;
    }

    .app-header__logo {
      font-weight: 700;
      font-size: 18px;
      color: #111827;
      text-decoration: none;
      letter-spacing: -0.02em;

      &:hover { color: #2563eb; }
    }

    .app-main {
      max-width: 1100px;
      margin: 0 auto;
      padding: 32px 24px;
    }
  `]
})
export class AppComponent {}
