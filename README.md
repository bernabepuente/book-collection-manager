# Book Collection Manager

A small full-stack app to manage a personal book collection. Built with Spring Boot on the backend and Angular 18 on the frontend.

---

## Getting started

### Backend

```bash
cd backend
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run
```

The API starts on **http://localhost:8080**.  
The H2 console is available at `/h2-console` (JDBC URL: `jdbc:h2:mem:books`, no password)  useful to inspect data during development.

### Frontend

```bash
cd frontend
npm install
npx ng serve
```

Open **http://localhost:4200**. CORS is handled server-side, no proxy config needed.

### Running tests

```bash
# Backend
cd backend && ./mvnw test           # Windows: .\mvnw.cmd test

# Frontend
cd frontend && npx ng test --watch=false
```

---

## Project structure

```
book-collection-manager/
 backend/
    src/main/java/com/bernabe/bookcollection/
       model/         Book entity (JPA)
       repository/    Spring Data JPA + title/author search
       service/       Business logic, ISBN normalisation, duplicate check
       controller/    REST endpoints (/api/books)
       exception/     BookNotFoundException, DuplicateIsbnException, GlobalExceptionHandler
       config/        CORS (allows localhost:4200)
    Dockerfile         Multi-stage build: Maven build  eclipse-temurin:17-jre-alpine
    pom.xml

 frontend/
    src/app/
        models/        Book interface
        services/      BookService (HttpClient) + spec
        components/
            book-list/ List + search bar + delete (signals for state)
            book-form/ Create/edit  same component, mode detected from route param

 .github/workflows/ci.yml   GitHub Actions CI + commented Azure deploy job
```

---

## API

| Method | Path | Response |
|--------|------|----------|
| GET | `/api/books` | 200  array; optional `?search=` filters by title or author |
| GET | `/api/books/{id}` | 200 or 404 |
| POST | `/api/books` | 201 on success · 400 validation error · 409 duplicate ISBN |
| PUT | `/api/books/{id}` | 200 · 400 · 404 · 409 (only if ISBN changed to an existing one) |
| DELETE | `/api/books/{id}` | 204 · 404 |

---

## Architecture  cloud deployment (Azure)

```
  Browser
  (Angular SPA  Azure Static Web Apps or CDN)
       
         HTTPS
       
  Azure Application Gateway   TLS termination, WAF
       
         HTTP
       
  Azure App Service  (containerised Spring Boot)
         pulls image from
        Azure Container Registry (ACR)
       
         JDBC / SSL
       
  Azure Database for PostgreSQL (Flexible Server)
```

**Local dev** uses H2 in-memory  zero setup. Moving to PostgreSQL only requires updating `spring.datasource.*` in `application.properties` and adding the Postgres JDBC driver to `pom.xml`.

**Deployment steps (Azure App Service):**

1. GitHub Actions builds the Docker image and pushes it to ACR (`docker build` + `docker push`).
2. `az webapp config container set` updates App Service to the new image tag.
3. App Service pulls the image and restarts automatically.
4. Angular static output is deployed to Azure Static Web Apps via `azure/static-web-apps-deploy`.
5. Application Gateway routes `/api/*` to App Service and `/*` to the static site.

See `.github/workflows/ci.yml` for the commented-out `deploy` job with exact steps and required secrets.

---

## Design decisions

**ISBN-13 only**  New books ship with 13-digit ISBNs. I decided not to validate the check digit (Luhn-like algorithm)  that felt like over-engineering for this scope and is easy to add later.

**ISBN normalisation in the service layer**  `BookService.normalizeIsbn()` strips hyphens and spaces before any duplicate check or DB write. So `978-0-13-235088-4` and `9780132350884` are treated as the same book. There is a comment in the entity so future developers understand why the column holds only digits.

**No Lombok**  Explicit getters/setters make the data model easier to read for someone jumping into the codebase cold. `@Data` would shorten the entity but obscure what it actually does.

**Angular signals instead of NgRx**  NgRx would be overkill for three API calls. Angular 18 signals give reactive state without boilerplate and are now the recommended approach for this kind of use case.

**No external UI library**  Bootstrap and Material both pull in styles I would spend time fighting. Plain SCSS with CSS custom properties gives full control and makes the design choices explicit.

**Lazy-loaded routes**  `loadComponent` is easy to set up and is the right habit for larger apps. It costs nothing at this scale.

---

## Trade-offs and known limitations

- **No authentication**  Adding Spring Security + JWT would be the obvious next step for a real app.
- **No pagination**  Loading all books at once is fine for a personal library. For anything larger: `Pageable` on the repository + page controls in the frontend.
- **H2 is in-memory**  Data resets on restart. Intentional for a demo; the production story is covered above.
- **ISBN check digit not validated**  The regex `^\d{13}$` checks format only, not mathematical validity.
- **No HTTPS locally**  TLS would terminate at the load balancer in production.

---

## CI / CD

`.github/workflows/ci.yml` runs on every push to `main`/`develop` and on PRs targeting `main`.

| Job | What it does |
|-----|-------------|
| `backend` | `mvn verify` (tests + compile)  `docker build` |
| `frontend` | `npm ci`  `ng test` (ChromeHeadless)  `ng build --configuration production` |
| `deploy` *(commented out, main only)* | `az login`  push to ACR  `az webapp config container set` |

Secrets required to activate the deploy job: `AZURE_CREDENTIALS`, `REGISTRY_LOGIN_SERVER`, `REGISTRY_USERNAME`, `REGISTRY_PASSWORD`, `AZURE_WEBAPP_NAME`.

---

## AI usage note

I used GitHub Copilot for boilerplate acceleration (JPA entity getters/setters, SCSS reset, `HttpTestingController` test scaffolding). All architectural decisions were made manually: the ISBN normalisation strategy, surfacing 409 conflicts as field-level Angular form errors, the `excludeId` pattern for update duplicate checks, the choice of signals over NgRx, and the Azure deployment design. The commit history reflects the actual development order.

---

*Built as a take-home exercise.*
