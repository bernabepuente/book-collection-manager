# Book Collection Manager

A small full-stack app to manage a personal book library. Built with Spring Boot on the backend and Angular 18 on the frontend.

---

## Getting started

### Backend

`ash
cd backend
./mvnw spring-boot:run
`

The API starts on **http://localhost:8080**. The H2 console is available at `/h2-console` (JDBC URL: `jdbc:h2:mem:books`, no password needed)  handy for a quick look at the data while developing.

### Frontend

`ash
cd frontend
npm install
npx ng serve
`

Open **http://localhost:4200**. The dev server proxies nothing special; CORS is handled on the Spring side.

### Running tests

`ash
# backend
cd backend && ./mvnw test

# frontend
cd frontend && npx ng test --watch=false
`

---

## Project structure

`
book-collection-manager/
 backend/          Spring Boot 3 + H2 + spring-boot-starter-validation
    src/main/java/com/bernabe/bookcollection/
       model/          Book entity (JPA)
       repository/     Spring Data JPA + custom search query
       service/        Business logic, ISBN normalisation, duplicate check
       controller/     REST endpoints
       exception/      Custom exceptions + @ControllerAdvice handler
       config/         CORS configuration
    Dockerfile          Multi-stage build (Maven  JRE-alpine)

 frontend/         Angular 18 standalone components, no external UI library
    src/app/
        models/         Book interface
        services/       BookService (HttpClient)
        components/
            book-list/  List + search + delete
            book-form/  Create / edit (shared component, mode from route param)

 .github/workflows/ci.yml   GitHub Actions  test & Docker build
`

---

## API

| Method | Path | Notes |
|--------|------|-------|
| GET | `/api/books` | Optional `?search=` query param |
| GET | `/api/books/{id}` | 404 if not found |
| POST | `/api/books` | 201 on success, 400 on validation failure, 409 on duplicate ISBN |
| PUT | `/api/books/{id}` | 409 only if ISBN actually changed to one that already exists |
| DELETE | `/api/books/{id}` | 204 no content |

---

## Architecture overview

```
  Browser
  (Angular SPA – static files on Azure Static Web Apps or a CDN)
       │
       │  HTTPS
       ▼
  Azure Application Gateway  ←─ TLS termination, WAF
       │
       │  HTTP
       ▼
  Azure App Service (containerised Spring Boot)
       │  pulls image from
       ├──────────────────► Azure Container Registry (ACR)
       │
       │  JDBC
       ▼
  Azure Database for PostgreSQL (Flexible Server)
```

**Local dev** uses H2 in-memory – no setup required. Swapping to PostgreSQL only needs a change in `application.properties` and adding the Postgres driver to `pom.xml`.

**Deployment flow (Azure):**

1. GitHub Actions builds the Docker image and pushes it to ACR.
2. App Service is configured to pull from ACR (`az webapp config container set`).
3. App Service restarts automatically on each new image tag.
4. The Angular build output (static files) is deployed to Azure Static Web Apps via the GitHub Actions `azure/static-web-apps-deploy` action.
5. Application Gateway sits in front of both services, handling TLS and routing `/api/*` to App Service and `/*` to the static site.

See `.github/workflows/ci.yml` for the commented-out deploy job with the exact steps and required secrets.

---

## Design decisions I made along the way

**ISBN-13 only**  The assignment didn't specify, but I went with 13-digit ISBNs because that's what new books ship with. I don't validate the check digit (the Luhn-like algorithm)  that felt like over-engineering for this scope, and it's easy to add later if needed.

**ISBN normalisation in the service layer**  Before any duplicate check or database write, `BookService.normalizeIsbn()` strips hyphens and spaces. So `978-0-13-235088-4` and `9780132350884` are treated as the same book. There's a comment in the entity explaining this so future developers don't wonder why the column has only digits.

**No Lombok**  I could have cut the entity down by a third with `@Data`, but I find it makes code harder to read for someone who jumps into the project cold. Explicit getters/setters make the data model obvious.

**Angular signals instead of NgRx**  NgRx would have been overkill for this size. Angular 18 signals give you reactive state without the ceremony, and they're now the recommended approach for simpler cases anyway.

**No UI component library**  Bootstrap and Material both pull in a lot of styles I'd spend time overriding. I went with plain SCSS and CSS custom properties for colours and spacing. It's easier to tweak and shows what the CSS actually does.

**Lazy loading routes**  Even with only two routes, `loadComponent` is easy to set up and is the right habit for larger apps. It costs nothing here.

---

## Trade-offs and known limitations

- **No authentication**  Out of scope for the assignment. Spring Security + JWT would be the obvious next step for anything real.
- **No pagination**  The book list loads everything at once. For a personal library this is fine. For anything larger I'd add `Pageable` to the repository and a page control in the frontend.
- **H2 is in-memory**  Data is lost on restart. Intentional for a demo; production story is above.
- **ISBN check digit not validated**  A 13-character regex only checks the format, not the mathematical validity of the ISBN.
- **No HTTPS locally**  The dev setup is plain HTTP. Production would terminate TLS at the load balancer.

---

## CI / CD

The GitHub Actions workflow (`.github/workflows/ci.yml`) has two active jobs and one commented-out deploy job:

| Job | Trigger | What it does |
|-----|---------|-------------|
| `backend` | every push / PR | `mvn verify` (tests + compile) → `docker build` |
| `frontend` | every push / PR | `npm ci` → `ng test` (ChromeHeadless) → `ng build --prod` |
| `deploy` *(commented out)* | push to `main` only | `az login` → push image to ACR → `az webapp config container set` |

To activate the deploy job, create these GitHub secrets and uncomment the job:
- `AZURE_CREDENTIALS` – service principal JSON (`az ad sp create-for-rbac --sdk-auth`)
- `REGISTRY_LOGIN_SERVER` – ACR login server (e.g. `myregistry.azurecr.io`)
- `REGISTRY_USERNAME` / `REGISTRY_PASSWORD` – ACR admin credentials
- `AZURE_WEBAPP_NAME` – name of the App Service web app

---

*Built as a take-home exercise.*
